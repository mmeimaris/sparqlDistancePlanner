package org.mapdb;

import java.io.DataInput;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;

/**
 * append only store
 */
public class StoreAppend extends Store {

    /** 2 byte store version*/
    protected static final int STORE_VERSION = 100;

    /** 4 byte file header */
    protected static final int HEADER = (0xAB3D<<16) | STORE_VERSION;


    protected static final long headerSize = 16;

    protected static final StoreAppend[] STORE_APPENDS_ZERO_ARRAY = new StoreAppend[0];


    protected WriteAheadLog wal;

    protected Volume headVol;


    /**
     * In memory table which maps recids into their offsets. Positive values are offsets.
     * Zero value indicates on-used records
     * Negative values are:
     * <pre>
     *     -1 - records was deleted, return null
     *     -2 - record has zero size
     *     -3 - null record, return null
     * </pre>
     *
     *
     */
    //TODO this is in-memory, move to temporary file or something
    protected Volume indexTable;

    protected final AtomicLong highestRecid = new AtomicLong(0);
    protected final boolean tx;

    protected final LongLongMap[] modified;

    protected final ScheduledExecutorService compactionExecutor;

    protected final Set<StoreAppend> snapshots;

    protected final boolean isSnapshot;

    protected final long startSize;
    protected final long sizeIncrement;
    protected final int sliceShift;

    protected StoreAppend(String fileName,
                          Volume.VolumeFactory volumeFactory,
                          Cache cache,
                          int lockScale,
                          int lockingStrategy,
                          boolean checksum,
                          boolean compress,
                          byte[] password,
                          boolean readonly,
                          boolean snapshotEnable,
                          boolean fileLockDisable,
                          DataIO.HeartbeatFileLock fileLockHeartbeat,
                          boolean txDisabled,
                          ScheduledExecutorService compactionExecutor,
                          long startSize,
                          long sizeIncrement
    ) {
        super(fileName, volumeFactory, cache, lockScale,lockingStrategy, checksum, compress, password, readonly,
                snapshotEnable,fileLockDisable, fileLockHeartbeat);
        this.tx = !txDisabled;
        if(tx){
            modified = new LongLongMap[this.lockScale];
            for(int i=0;i<modified.length;i++){
                modified[i] = new LongLongMap();
            }
        }else{
            modified = null;
        }
        this.compactionExecutor = compactionExecutor;
        this.snapshots = Collections.synchronizedSet(new HashSet<StoreAppend>());
        this.isSnapshot = false;
        this.sizeIncrement = Math.max(1L<<CC.VOLUME_PAGE_SHIFT, DataIO.nextPowTwo(sizeIncrement));
        this.startSize = Fun.roundUp(Math.max(1L<<CC.VOLUME_PAGE_SHIFT,startSize), this.sizeIncrement);
        this.sliceShift = Volume.sliceShiftFromSize(this.sizeIncrement);

    }

    public StoreAppend(String fileName) {
        this(fileName,
                fileName==null? CC.DEFAULT_MEMORY_VOLUME_FACTORY : CC.DEFAULT_FILE_VOLUME_FACTORY,
                null,
                CC.DEFAULT_LOCK_SCALE,
                0,
                false,
                false,
                null,
                false,
                false,
                false,
                null,
                false,
                null,
                0,
                0
        );
    }

    /** protected constructor used to take snapshots*/
    protected StoreAppend(StoreAppend host, LongLongMap[] uncommitedData){
        super(null, null,null,
                host.lockScale,
                Store.LOCKING_STRATEGY_NOLOCK,
                host.checksum,
                host.compress,
                null, //TODO password on snapshot
                true, //snapshot is readonly
                false,
                false,
                null);

        indexTable = host.indexTable;
        this.wal = host.wal;


        //replace locks, so reads on snapshots are not performed while host is updated
        for(int i=0;i<locks.length;i++){
            locks[i] = host.locks[i];
        }

        tx = true;
        modified = new LongLongMap[this.lockScale];
        if(uncommitedData==null){
            for(int i=0;i<modified.length;i++) {
                modified[i] = new LongLongMap();
            }
        }else{
            for(int i=0;i<modified.length;i++) {
                Lock lock = locks[i].writeLock();
                lock.lock();
                try {
                    modified[i] = uncommitedData[i].clone();
                }finally {
                    lock.unlock();
                }
            }
        }

        this.compactionExecutor = null;
        this.snapshots = host.snapshots;
        this.isSnapshot = true;
        host.snapshots.add(StoreAppend.this);
        this.startSize = host.startSize;
        this.sizeIncrement = host.sizeIncrement;
        this.sliceShift = host.sliceShift;
    }

    @Override
    public void init() {
        super.init();
        structuralLock.lock();
        try {
            boolean empty = Volume.isEmptyFile(fileName+".wal.0"); //TODO do not use file name

            wal = new WriteAheadLog(fileName, volumeFactory, makeFeaturesBitmap());

//            vol = volumeFactory.makeVolume(fileName, readonly,fileLockDisable,sliceShift, startSize, false);
//            eof = headerSize;
            indexTable = new Volume.ByteArrayVol(CC.VOLUME_PAGE_SHIFT,0L);
            indexTable.ensureAvailable(RECID_LAST_RESERVED*8);

            for (int i = 0; i <= RECID_LAST_RESERVED; i++) {
                indexTable.ensureAvailable(i * 8);
                indexTable.putLong(i * 8, -3);
            }

            if (empty) {
                initCreate();
            } else {
                initOpen();
            }
        }finally {
            structuralLock.unlock();
        }
    }

    protected void initCreate() {
        headVol = volumeFactory.makeVolume(fileName, false,true);
        headVol.ensureAvailable(16);
        headVol.putInt(0,HEADER);
        headVol.putLong(8, makeFeaturesBitmap());
        headVol.sync();
        wal.open(WriteAheadLog.NOREPLAY);
//        wal.startNextFile();
        for(long recid=1;recid<=Store.RECID_LAST_RESERVED;recid++){
            wal.walPutPreallocate(recid);
        }
        wal.commit();
//        wal.seal();
//        wal.startNextFile();
        highestRecid.set(RECID_LAST_RESERVED);

//        vol.ensureAvailable(headerSize);
//
//        vol.putInt(0,HEADER);
//        long feat = makeFeaturesBitmap();
//        vol.putLong(HEAD_FEATURES, feat);
//        vol.sync();
    }

    protected void initOpen() {
        headVol = volumeFactory.makeVolume(fileName, false,true);
        if(headVol.getInt(0)!=HEADER){
            //TODO handle version numbers
            throw new DBException.DataCorruption("Wrong header at:"+fileName);
        }

        //TODO lock all for write

        long featuresBitMap = headVol.getLong(8);
        checkFeaturesBitmap(featuresBitMap);

        final AtomicLong highestRecid2 = new AtomicLong(RECID_LAST_RESERVED);

        final WriteAheadLog.WALReplay replay = new WriteAheadLog.WALReplay() {
            @Override
            public void beforeReplayStart() {

            }

            @Override
            public void afterReplayFinished() {

            }

            @Override
            public void writeLong(long offset, long value) {
                throw new DBException.DataCorruption();
            }

            @Override
            public void writeByteArray(long offset, long walId, Volume vol, long volOffset, int length) {
                throw new DBException.DataCorruption();
            }

            @Override
            public void writeRecord(long recid, long walId, Volume vol, long volOffset, int length) {
                highestRecid2.set(Math.max(highestRecid2.get(),recid));
                long recidOffset = recid*8;
                indexTable.ensureAvailable(recidOffset + 8);
                indexTable.putLong(recidOffset, walId);
            }

            @Override
            public void commit() {

            }

            @Override
            public void rollback() {
                throw new DBException.DataCorruption();
            }

            @Override
            public void writeTombstone(long recid) {
                indexTable.ensureAvailable(recid*8+8);
                indexTable.putLong(recid*8,-1);
            }

            @Override
            public void writePreallocate(long recid) {
                indexTable.ensureAvailable(recid*8+8);
                indexTable.putLong(recid*8,-3);
            }
        };
        wal.open(replay);

        highestRecid.set(highestRecid2.get());
    }

    @Override
    protected <A> A get2(long recid, Serializer<A> serializer) {
        if(CC.ASSERT)
            assertReadLocked(lockPos(recid));

        long walId= tx?
                modified[lockPos(recid)].get(recid):
                0;
        if(walId==0) {
            try {
                walId = indexTable.getLong(recid * 8);
            } catch (ArrayIndexOutOfBoundsException e) {
                //TODO this code should be aware if indexTable internals?
                throw new DBException.EngineGetVoid();
            }
        }

        if(walId==0){
            throw new DBException.EngineGetVoid();
        }
        if(walId==-1||walId==-3)
            return null;

        byte[] b = wal.walGetRecord(walId,recid);
        if(b==null)
            return null;
        DataInput input = new DataIO.DataInputByteArray(b);
        return deserialize(serializer, b.length, input);
    }

    @Override
    protected void update2(long recid, DataIO.DataOutputByteArray out) {
        insertOrUpdate(recid, out, false);
    }

    private void insertOrUpdate(long recid, DataIO.DataOutputByteArray out, boolean isInsert) {
        if(CC.ASSERT)
            assertWriteLocked(lockPos(recid));

        //TODO assert indexTable state, record should already exist/not exist

        long walId = wal.walPutRecord(recid, out==null?null:out.buf, 0, out==null?0:out.pos);
        indexTablePut(recid, walId);
    }

    @Override
    protected <A> void delete2(long recid, Serializer<A> serializer) {
        if(CC.ASSERT)
            assertWriteLocked(lockPos(recid));

        wal.walPutTombstone(recid);

        indexTablePut(recid, -1); // -1 is deleted record
    }

    @Override
    public long getCurrSize() {
        return 0;
    }

    @Override
    public long getFreeSize() {
        return 0;
    }

    @Override
    public boolean fileLoad() {
        return wal.fileLoad();
    }

    @Override
    public long preallocate() {
        long recid = highestRecid.incrementAndGet();
        Lock lock = locks[lockPos(recid)].writeLock();
        lock.lock();
        try{
            wal.walPutPreallocate(recid);
            indexTablePut(recid,-3);
        }finally {
            lock.unlock();
        }

        return recid;
    }

    protected void indexTablePut(long recid, long walId) {
        if(CC.ASSERT)
            assertWriteLocked(lockPos(recid));
        if(tx){
            modified[lockPos(recid)].put(recid,walId);
        }else {
            indexTable.ensureAvailable(recid*8+8);
            indexTable.putLong(recid * 8, walId);
        }
    }

    @Override
    public <A> long put(A value, Serializer<A> serializer) {
        DataIO.DataOutputByteArray out = serialize(value,serializer);
        long recid = highestRecid.incrementAndGet();
        int lockPos = lockPos(recid);
        Cache cache = caches==null ? null : caches[lockPos] ;
        Lock lock = locks[lockPos].writeLock();
        lock.lock();
        try{
            if(cache!=null) {
                cache.put(recid, value);
            }

            insertOrUpdate(recid,out,true);
        }finally {
            lock.unlock();
        }

        return recid;
    }

    @Override
    public void close() {
        if(closed)
            return;
        commitLock.lock();
        try {
            if(closed)
                return;

            if(isSnapshot){
                snapshots.remove(this);
                return;
            }

            if(!readonly) {
                if (tx)
                    wal.rollback();
                wal.seal();
            }
            wal.close();
            indexTable.close();
            headVol.close();

            if(caches!=null){
                for(Cache c:caches){
                    c.close();
                }
                Arrays.fill(caches,null);
            }
            if(fileLockHeartbeat !=null) {
                fileLockHeartbeat.unlock();
                fileLockHeartbeat = null;
            }
            closed = true;
        }finally{
            commitLock.unlock();
        }
    }

    @Override
    public void commit() {
        if(isSnapshot)
            return;

        if(!tx){
            wal.commit();
            return;
        }

        commitLock.lock();
        try{
            StoreAppend[] snaps = snapshots==null ?
                    STORE_APPENDS_ZERO_ARRAY :
                    snapshots.toArray(STORE_APPENDS_ZERO_ARRAY);

            for(int i=0;i<locks.length;i++) {
                Lock lock = locks[i].writeLock();
                lock.lock();
                try {
                    long[] m = modified[i].table;
                    for(int j=0;j<m.length;j+=2){
                        long recid = m[j];
                        long recidOffset = recid*8;
                        if(recidOffset==0)
                            continue;
                        indexTable.ensureAvailable(recidOffset + 8);
                        long oldVal = indexTable.getLong(recidOffset);
                        indexTable.putLong(recidOffset,m[j+1]);

                        for(StoreAppend snap:snaps){
                            LongLongMap m2 = snap.modified[i];
                            if(m2.get(recid)==0) {
                                m2.put(recid, oldVal);
                            }
                        }
                    }
                    modified[i].clear();
                }finally {
                    lock.unlock();
                }
            }
            wal.commit();

        }finally {
            commitLock.unlock();
        }
    }

    @Override
    public void rollback() throws UnsupportedOperationException {
        if(!tx || readonly || isSnapshot)
            throw new UnsupportedOperationException();
        commitLock.lock();
        try{
            for(int i=0;i<locks.length;i++) {
                Lock lock = locks[i].writeLock();
                lock.lock();
                try {
                    modified[i].clear();
                }finally {
                    lock.unlock();
                }
            }
            wal.rollback();
        }finally {
            commitLock.unlock();
        }
    }



    @Override
    public boolean canRollback() {
        return tx;
    }

    @Override
    public boolean canSnapshot() {
        return true;
    }

    @Override
    public Engine snapshot() throws UnsupportedOperationException {
        commitLock.lock();
        try {
            return new StoreAppend(this, modified);
        }finally {
            commitLock.unlock();
        }
    }


    @Override
    public void compact() {
        if(isSnapshot)
            return;

    }


    @Override
    public void backup(OutputStream out, boolean incremental) {
        //TODO full backup
        throw new UnsupportedOperationException("not yet implemented");
    }


    @Override
    public void backupRestore(InputStream[] in) {
        //TODO full backup
        throw new UnsupportedOperationException("not yet implemented");
    }
}
