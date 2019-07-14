package com.example.rxandlambda;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.BehaviorSubject;

/**
 * https://www.jianshu.com/p/823252f110b0
 */
public class MainActivity extends AppCompatActivity {

    private Button button ;
    private Button button2 ;
    private Button button3 ;
    private Button button4 ;
    private Button button5 ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        button5 = findViewById(R.id.button5);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Observable<Integer> observable = Observable.create(emitter -> {
                    emitter.onNext(1);
                    emitter.onNext(2);
                    emitter.onNext(3);
                });
                //感觉好像上游的数据(源)传递进了这个Lambda表达式
                observable.subscribe(System.out::println);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //forEach 订阅并接收每个元素
                //map 起到转换的作用，将range序列发送的int型数据，转化为字符串类型
                //**重点**：感觉这些操作符使用Lambda的时候，操作的东西都是“隐藏，看不见，自动”的传递进来的，经过操作符再操作就可以了
                Observable.range(0,10).map(String::valueOf).forEach(System.out::println);
                //
            }
        });

        //defer 直到有观察者订阅时候才创建Observable，并且为每个观察者穿件一个新的observable
//        Observable<Long> observable = Observable.defer(new Callable<ObservableSource<Long>>() {
//            @Override
//            public ObservableSource<Long> call() throws Exception {
//                return Observable.just(System.currentTimeMillis());
//            }
//        });
//        observable.subscribe(System.out::print);
//        System.out.println();
//        observable.subscribe(System.out::print);

        //***********from 系列
        /**from 系列的方法用来从指定的数据源中获取一个Observable
         *      1.fromArray 从数组中获取
         *      2.fromCallable 从Callable中获取
         *      3.fromFuture从Future中获取，有多个重载版本，可以用来指定现场和超时等信息
         */






        //just系列的方法区别在于接受参数的个数不同，最少一个最多10个







        //timer 创建一个在给定的时间段之后返回一个特殊值得Observable，它在延迟一段时间给定的时间后发射一个简单的数字0
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("songbl","===");

                Observable.timer(1,TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        Log.e("song","=="+aLong);
                    }
                });

            }
        });

        //***********************  转换    *********************
        //1. map：对原始Observable发射的每一项数据应用一个你选择的函数，然后返回一个发射这些结果的Observable
        //对于参数 ：map(Function<? super T, ? extends R> mapper) ，一个是原始的数据类型，一个表示要转换之后的数据类型
        //转换逻辑卸载接口的实现方法中即可（下例中的Object可以写成Object）
        Observable.range(1, 5).map(new Function<Integer, Object>() {
            @Override
            public Object apply(Integer integer) throws Exception {
                return integer;
            }
        }).subscribe(new Observer<Object>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Object o) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
        //简写为：Observable.range(1, 5).map(String::valueOf).subscribe(System.out::println);


        //***********flapMap 使用解析
        //将一个发送事件的上游Observable变换为多个发送事件的Observables，然后
        //将他们的事件合并后放进一个单独的Observable里。
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Observable.range(1, 5)
                        .flatMap(new Function<Integer, ObservableSource<String>>() {//最后组成一个Observable
                            @Override
                            public ObservableSource<String> apply(Integer i) throws Exception {
                                //构成多个单独的Observable
                                return Observable.just(String.valueOf(i));
                            }
                        })
                        .subscribe(System.out::println);
            }
        });



        //***********************  过滤   *********************
        //filter 根据指定的规则对源数据进行过滤
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Observable.range(1,10).filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) throws Exception {
                        return integer > 5;
                    }
                }).subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.e("song","=="+integer);
                    }
                });
            }
        });
        BehaviorSubject<String> behaviorSubject = BehaviorSubject.createDefault("behaviorSubject1");
        //这地方可以绑定之类的
        behaviorSubject.onNext("bSubject2");
        //获取最近一次发生的数据
        behaviorSubject.getValue();



        //**********************======************************
        //理解:map返回的直接是结果集
        //flapMap返回的是ObservableSource<Integer>，其实就是Observable，然后这个Observable再发送数据，
        // 组成一个Observable（就是flapMap返回的 final <R> Observable<R>），再继续向下传递数据

        Observable.range(1,5).map(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) throws Exception {
                //map的话，直接将结果继续向下发送就完了
                return integer+"";
            }
        }).flatMap(new Function<String, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(String s) throws Exception {
                  //被转化为多个时，一般利用from/just进行一一分发
                // (2. 将这些数据发送到一个Observable中，然后这个Observable再继续向下发送数据)
                return Observable.just(Integer.valueOf(s));
            }
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.e("song","=="+integer);
            }
        });
    }
}
