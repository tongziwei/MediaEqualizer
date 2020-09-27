package com.goertek.mediaequalizer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {
    private static final float VISUALIZER_HEIGHT_DIP = 50f;
    private List<String> permissions = new ArrayList<String>();
    private LinearLayout mLlMain;
    private TextView mTvStatus;
    private AnalogController mBassControl;
    private AnalogController m3DControl;

    private MediaPlayer mMediaPlayer;
    private Equalizer mEqualizer;
    private Visualizer mVisualizer;
    private VisualizerView mVisualizerView;
    private VisualizerFftView mVisualizerFftView;
    private BassBoost mBassBoost;
    private PresetReverb mPresetReverb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLlMain = (LinearLayout)findViewById(R.id.ll_main);
        mTvStatus = (TextView)findViewById(R.id.tv_status);
        mBassControl = (AnalogController)findViewById(R.id.controllerBass);
        m3DControl = (AnalogController)findViewById(R.id.controller3D);

        askPermission();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mMediaPlayer = MediaPlayer.create(this,R.raw.lenka);
       if(mMediaPlayer ==null){
           return;
       }

        setupBassAnd3D();
        setupVisualizer();
        setupEqualizer();
        mVisualizer.setEnabled(true);

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
               mVisualizer.setEnabled(false);
            }
        });
        mMediaPlayer.start();
        mTvStatus.setText("播放中");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mMediaPlayer!=null ){
            mEqualizer.release();
            mVisualizer.release();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void setupBassAnd3D(){
         mBassBoost = new BassBoost(0,mMediaPlayer.getAudioSessionId());
         mBassBoost.setEnabled(true);
     /*   BassBoost.Settings bassBoostSettingTemp = mBassBoost.getProperties();
        BassBoost.Settings bassBoostSetting = new BassBoost.Settings(bassBoostSettingTemp.toString());
        bassBoostSetting.strength = Settings.equalizerModel.getBassStrength();
        mBassBoost.setProperties(bassBoostSetting);*/
        mBassControl.setLabel("BASS");
        int x =0;
        if (mBassBoost != null) {
            try {
                x = ((mBassBoost.getRoundedStrength() * 19) / 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (x == 0) {
            mBassControl.setProgress(1);
        } else {
            mBassControl.setProgress(x);
        }
        mBassControl.setOnProgressChangedListener(new AnalogController.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                  short roundStrength = (short) (((float) 1000 / 19) * (progress));  //控件共有19个进度点
                  mBassBoost.setStrength(roundStrength);
            }
        });


        mPresetReverb = new PresetReverb(0,mMediaPlayer.getAudioSessionId());
        mPresetReverb.setEnabled(true);
        m3DControl.setLabel("3D");
        int y=0;
        if (mPresetReverb != null) {
            try {
                y = (mPresetReverb.getPreset() * 19) / 6;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (y == 0) {
            m3DControl.setProgress(1);
        } else {
            m3DControl.setProgress(y);
        }
        m3DControl.setOnProgressChangedListener(new AnalogController.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                short reverbPreset = (short) ((progress * 6) / 19);
                mPresetReverb.setPreset(reverbPreset);
            }
        });
    }

    private void setupEqualizer(){
        mEqualizer = new Equalizer(0,mMediaPlayer.getAudioSessionId());

        mEqualizer.setEnabled(true);
        short bands = mEqualizer.getNumberOfBands();
        // getBandLevelRange 是一个数组，返回一组频谱等级数组，
        // 第一个下标为最低的限度范围
        // 第二个下标为最大的上限,依次取出
        final short minEqualizer = mEqualizer.getBandLevelRange()[0];
        short maxEqualizer = mEqualizer.getBandLevelRange()[1];

        for(short i=0;i<bands;i++){
            final short band = i;

            TextView freqTextView = new TextView(this);
            freqTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
            freqTextView.setGravity(Gravity.CENTER);

            int centerFreq = mEqualizer.getCenterFreq(band); //取出中心频率,单位：毫赫兹
            freqTextView.setText((centerFreq/1000)+"Hz");

            mLlMain.addView(freqTextView);

            LinearLayout mLlRow = new LinearLayout(this);
            mLlRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
            mLlRow.setOrientation(LinearLayout.HORIZONTAL);

            TextView minEqTextView = new TextView(this);
            minEqTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
            minEqTextView.setText((minEqualizer/100)+"dB");

            TextView maxEqTextView = new TextView(this);
            maxEqTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
            maxEqTextView.setText((maxEqualizer/100)+"dB");

            SeekBar seekBar = new SeekBar(this);
            LinearLayout.LayoutParams seekLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            seekLayoutParams.weight =1;
            seekBar.setLayoutParams(seekLayoutParams);
            seekBar.setMax(maxEqualizer-minEqualizer);
            seekBar.setProgress(mEqualizer.getBandLevel(band));
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    mEqualizer.setBandLevel(band,(short)(i+minEqualizer));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            mLlRow.addView(minEqTextView);
            mLlRow.addView(seekBar);
            mLlRow.addView(maxEqTextView);

            mLlMain.addView(mLlRow);

        }



    }

    private void setupVisualizer(){
        mVisualizerView = new VisualizerView(this);
        mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                (int) (VISUALIZER_HEIGHT_DIP * getResources()
                        .getDisplayMetrics().density)));

        mLlMain.addView(mVisualizerView);

        mVisualizerFftView = new VisualizerFftView(this);
        mVisualizerFftView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
               ViewGroup.LayoutParams.WRAP_CONTENT ));
        mLlMain.addView(mVisualizerFftView);

        mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());

       //设置每次捕获频谱的大小，音乐在播放中的时候采集的数据的大小或者说是采集的精度吧，我的理解，而且getCaptureSizeRange()
        //	所返回的数组里面就两个值 .文档里说数组[0]是最小值（128），数组[1]是最大值（1024）。
       mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        //接下来就好理解了设置一个监听器来监听不断而来的所采集的数据。
        //一共有4个参数，第一个是监听者，第二个单位是毫赫兹，表示的是采集的频率，第三个是是否采集波形，第四个是是否采集频率
       mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
           @Override
           public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int i) {
               mVisualizerView.updateVisualizer(bytes);
           }

           @Override
           public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int i) {
               mVisualizerFftView.updateVisualizer(bytes);

           }
       }, Visualizer.getMaxCaptureRate()/2,true,true);

    }



    private boolean askPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int RECORD_AUDIO = checkSelfPermission( Manifest.permission.RECORD_AUDIO );
            if (RECORD_AUDIO != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }

            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), 1);
            } else
                return false;
        } else
            return false;
        return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {

            boolean result = true;
            for (int i = 0; i < permissions.length; i++) {
                result = result && grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
            if (!result) {

                Toast.makeText(this, "授权结果（至少有一项没有授权），result="+result, Toast.LENGTH_LONG).show();
                // askPermission();
            } else {
                //授权成功
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



}
