package com.bj.newsfastget.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by dd on 2016/10/12.
 */

public class ReadManager {

    private static ReadManager instance;
    public static ReadManager getManager(){
        if (instance==null){
            instance=new ReadManager();
        }
        return instance;
    }

    public ReadDetail getItem(long content_id){
        ReadDetail item= null;
        try {
            Realm mRealm= Realm.getDefaultInstance();
            ReadDetail realmObject=mRealm.where(ReadDetail.class).equalTo("content_id",content_id).findFirst();
            if (realmObject!=null)
                item = mRealm.copyFromRealm(realmObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }

    public List<ReadDetail> getItemList(){
        Realm mRealm= Realm.getDefaultInstance();
        RealmResults<ReadDetail> items = mRealm.where(ReadDetail.class).findAll();
        List<ReadDetail> itemList=mRealm.copyFromRealm(items);
        List<RealmObject> realmObjectList=new ArrayList<>();
        return itemList;
    }

    public void saveTalkList(final List<ReadDetail> itemList, final boolean isClean){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Realm mRealm= Realm.getDefaultInstance();
                if (isClean) {
                    final RealmResults<ReadDetail> talks=  mRealm.where(ReadDetail.class).findAll();
                    mRealm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            //删除所有数据
                            talks.deleteAllFromRealm();
                        }
                    });
                }
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mRealm.beginTransaction();
                mRealm.copyFromRealm(itemList);
                mRealm.commitTransaction();
            }
        }).start();
    }

    private void addItem(final ReadDetail item) {
        Realm mRealm= Realm.getDefaultInstance();
        RealmAsyncTask addTask=  mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(item);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
            }
        });

    }

}
