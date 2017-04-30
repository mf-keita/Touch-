package com.tenpa_mf.surfaceviewex;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by 慶太 on 2017/04/28.
 */

public class SurfaceViewView extends SurfaceView implements SurfaceHolder.Callback, Runnable{

    //画面定数
    private final static int
            W= 960,//画面幅
            H= 1600;//画面高さ
    private float scale;

    public SurfaceHolder holder;
    public Thread thread;
    public  Graphics g;
    private Bitmap[] tch = new Bitmap[5];
    //public Bitmap image;
    final static int S_TITLE=0,S_PLAY=1,S_GAMEOVER=2;
    public int init = S_TITLE;
    public int scene = S_TITLE;
    public int score = 0;
    public  int count = 0;
    public int touchPointX,touchPointY;
    public int dangerPointX,dangerPointY,samplePointX,samplePointY;




    public List<Point> sample=new ArrayList<Point>();//青標的
    public List<Point> bom = new ArrayList<Point>();//爆弾


    //コンストラクタ
    public SurfaceViewView(Activity activity){
        super(activity);

        //画像の読み込み
        //ビットマップの読み込み
        for(int i = 0; i<=4;i++){
            tch[i] = readBitmap(activity,"tch"+i);
        }

        //サーフェイスフォルダーの生成
        //サーフェイスというのは、画面上にさまざまなグラフィックを描画し表示するためのもので、
        // SurfaceViewではコンポーネント上にサーフェイスでさまざまな描画を表示する.
        holder = getHolder();
        holder.addCallback(this);

        //画面サイズの指定
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        int dh = W*p.y/p.x;

        //グラフィックスの生成
        g=new Graphics(W,dh,holder);
        g.setOrigin(0,(dh-H)/2);

    }

    //サーフェイス生成時に呼ばれる。
    public void surfaceCreated(SurfaceHolder holder){
        thread =new Thread(this);
        thread.start();
    }

    //サーフェイス終了時に呼ばれる。
    public void surfaceDestroyed(SurfaceHolder holder){thread=null;}

    //サーフェイス変更時に呼ばれる。
    public void surfaceChanged(SurfaceHolder holder,int format,int w,int h){};

    public void run(){
        while (thread!=null){
                //初期化
            if(init>=0){
                scene = init;

                //タイトルシーン
                if(scene ==S_TITLE){
                    sample.clear();
                }
                //ゲームオーバー
                else if(scene==S_GAMEOVER){
                    //ここに処理記載
                }
                init = -1;
            }
            //プレイ時の処理
            if(scene==S_PLAY){
                //標的出現までのカウント
                count++;
                if(count>30){
                    if(score<100) {
                        count = 0;
                        sample.add(new Point(rand(getWidth()), rand(getHeight()+50)));
                    }else {
                        count = 0;
                        sample.add((new Point(rand(getWidth()),rand(getHeight()+50))));
                        if((rand(3)+1)/3==1) {
                            bom();
                        }
                    }
                }

                //タッチで消去
                for(int i=sample.size()-1;i>=0;i--){
                    Point pos = sample.get(i);
                    if(Math.abs(pos.x-touchPointX)<100&&Math.abs(pos.y-touchPointY)<100){
                        sample.remove(i);
                        score +=10;
                    }
                }
                //タッチで爆発
                for(int j=bom.size()-1;j>=0;j--){
                    Point pos = bom.get(j);
                    if(Math.abs(pos.x-touchPointX)<50&&Math.abs(pos.y-touchPointY)<50){
                        bom.remove(j);
                        init = S_GAMEOVER;
                        score = 0;
                    }
                }
            }

            //canvasロック
            g.lock();

            //背景の描画
            g.drawBitmap(tch[0],0,0);

            //標的の描画
            for(int i = sample.size()-1;i>=0;i--){
                Point pos = sample.get(i);
                samplePointX = pos.x;
                samplePointY = pos.y;
                g.drawBitmap(tch[3],samplePointX,samplePointY);


            }

            //爆弾の描画
            for(int i = bom.size()-1;i>=0;i--){
                Point pos1 = bom.get(i);
                dangerPointX = pos1.x;
                dangerPointY = pos1.y;
                //爆弾と標的の座標が近ければ、爆弾非表示
                if(Math.abs(dangerPointX-samplePointX)<100&&Math.abs(dangerPointY-samplePointY)<100){
                    bom.remove(i);
                }else {
                    g.drawBitmap(tch[4], dangerPointX, dangerPointY);
                }
            }

            //スコアが300点ごとに、画面から爆弾掃除
            if(score%300==0){
                bom.clear();
            }

            //メッセージの描画
            if(scene==S_TITLE){
                g.drawBitmap(tch[1],(W-440)/2,700);
            }else if(scene == S_GAMEOVER){
                g.drawBitmap(tch[2],(W-340)/2,700);
                sample.clear();
                bom.clear();
            }
            //スコアの描画
            g.setColor(Color.WHITE);
            g.setTextSize(60);
            g.drawText("score:"+num2str(score,6),10,10+g.getOriginY()-(int)g.getFontMetrics().ascent);
            g.unlock();

        }
    }

    //タッチ時に呼ばれる処理
    public boolean onTouchEvent(MotionEvent event){
        int touchX = (int)event.getX()*W/getWidth();
        int touchY = (int)event.getY()*H/getHeight();
        int touchAction = event.getAction();
        if(touchAction==S_TITLE){
            init = S_PLAY;
        }
        //プレイシーンでのタッチアクション
        else if(scene==S_PLAY){
            //タッチ座標の取得
            touchPointX = touchX;
            touchPointY = touchY;
        }
        return true;
    }

    //Bitmapの読み込み
    private static Bitmap readBitmap(Context context, String name){
        int resID = context.getResources().getIdentifier(name,"drawable",context.getPackageName());
        return BitmapFactory.decodeResource(context.getResources(),resID);
    }

    //乱数取得のための関数
    private static Random rand = new Random();
    private static int rand(int num){
        return (rand.nextInt()>>>1)%num;
    }

    //数値から文字列への変換
    private static String num2str(int num,int len){
        String str = ""+num;
        while(str.length()<len){
            str = "0"+str; //0を頭に付けている
        }
        return str;
    }

    //爆弾
    public void bom(){
        bom.add(new Point(rand(getWidth()), rand(getHeight()+50)));
    }

}