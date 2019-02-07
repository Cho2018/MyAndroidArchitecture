package com.tistory.black_jin0427.myandroidarchitecture.view.main;

import android.util.Log;

import com.tistory.black_jin0427.myandroidarchitecture.api.GithubApi;
import com.tistory.black_jin0427.myandroidarchitecture.api.GithubApiProvider;
import com.tistory.black_jin0427.myandroidarchitecture.api.model.User;
import com.tistory.black_jin0427.myandroidarchitecture.constant.Constant;
import com.tistory.black_jin0427.myandroidarchitecture.room.UserDao;
import com.tistory.black_jin0427.myandroidarchitecture.room.UserDatabaseProvider;
import com.tistory.black_jin0427.myandroidarchitecture.rxEventBus.RxEvent;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class MainPresenter implements MainContract.Presenter {

    GithubApi api;
    MainContract.View view;

    private CompositeDisposable disposable;

    MainPresenter() {
        this.api = GithubApiProvider.provideGithubApi();
        this.disposable = new CompositeDisposable();
    }

    @Override
    public void setView(MainContract.View view) {
        this.view = view;
    }

    @Override
    public void releaseView() {
        disposable.clear();
    }

    @Override
    public void loadData() {

        disposable.add(api.getUserList(Constant.RANDOM_USER_URL)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> {
                    view.showProgress();
                })
                .doOnTerminate(() -> {
                    view.hideProgress();
                })
                .subscribe(userResponse -> {
                    view.setItems((ArrayList<User>)userResponse.userList);
                }, error -> {
                    Log.e("MyTag",error.getMessage());
                })
        );

    }

    @Override
    public void setRxEvent(UserDao userDao) {

        disposable.add(
                RxEvent.getInstance()
                        .getObservable()
                        .subscribe(
                                object -> {
                                    if(object instanceof User) {
                                        view.updateView((User) object);

                                        addUser(userDao, (User) object);
                                    }
                                },
                                error -> {
                                    Log.d("MyTag","onError");
                                },
                                () -> {
                                    Log.d("MyTag","onCompleted");
                                }
                        )
        );
    }

    @Override
    public void addUser(UserDao userDao, User user) {

        disposable.add(
                Observable.just(user)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        item -> {
                            Log.d("MyTag","item : " + item + " 저장");
                            userDao.add(item);
                        },
                        error -> {
                            Log.d("MyTag","onError");
                        },
                        () -> {
                            Log.d("MyTag","onCompleted");
                        }
                )
        );

    }
}