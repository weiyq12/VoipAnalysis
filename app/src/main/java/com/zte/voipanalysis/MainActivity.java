package com.zte.voipanalysis;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.zte.voipanalysis.view.InnerScrollView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity implements View.OnClickListener
{
    public static final String TAG = "MainActivity";

    private final int MSG_HIDE_SYNC_PB = 1;
    private final int MSG_SHOW_SYNC_PB = 2;
    private final int MSG_HIDE_SELECT_PB = 3;
    private final int MSG_SHOW_SELECT_PB = 4;
    private final int MSG_HIDE_STATIS_PB = 5;
    private final int MSG_SHOW_STATIS_PB = 6;
    private final int MSG_HIDE_DETAIL_PB = 7;
    private final int MSG_SHOW_DETAIL_PB = 8;

    private ScrollView sv_scroll;
    private TextView tv_title;
    private Button btn_one_key_start;
    private Button btn_one_key_save;

    private LinearLayout ll_sync;
    private EditText et_sync_start_date;
    private EditText et_sync_end_date;
    private InnerScrollView sv_download;
    private TextView tv_sync_info_display;
    private ProgressBar pb_sync_progress;
    private Button btn_sync_start;

    private LinearLayout ll_select;
    private EditText et_select_start_date;
    private EditText et_select_start_time;
    private EditText et_select_end_date;
    private EditText et_select_end_time;
    private EditText et_select_caller_account;
    private EditText et_select_receiver_account;
    private EditText et_select_talk_min_second;
    private EditText et_select_talk_max_second;
    private CheckBox cb_select_android;
    private CheckBox cb_select_ios;
    private CheckBox cb_select_windows;
    private InnerScrollView sv_select;
    private TextView tv_select_display;
    private ProgressBar pb_select_progress;
    private Button btn_select_start;

    private RadioButton rb_order_by_count;
    private RadioButton rb_order_by_rate;
    private RadioButton rb_order_by_account;
    private InnerScrollView sv_statis;
    private TextView tv_statis_info_display;
    private ProgressBar pb_statis_progress;
    private Button btn_statis_start;

    private EditText et_detail_account;
    private InnerScrollView sv_detail;
    private TextView tv_detail_info_display;
    private ProgressBar pb_detail_progress;
    private Button btn_detail_start;

    private Handler handler;

    private SyncDataThread syncDataThread;
    private SelectDataThread selectDataThread;
    private StatisDataThread statisDataThread;
    private DetailThread detailThread;

    private boolean isSyncDataChanged;
    private boolean isSelectDataChanged;
    private boolean isOneKeyStart;
    private int orderType;

    private StringBuffer syncInfoBuffer;
    private StringBuffer selectInfoBuffer;
    private StringBuffer statisInfoBuffer;
    private StringBuffer detailInfoBuffer;

    private List<RecordInfo> recordInfoList;
    private Map<String, List<RecordInfo>> map;
    private List<String> statisPrintList;

    private SimpleDateFormat dayFormat;
    private SimpleDateFormat timeFomat;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化组件
        initWidget();
        // 初始化数据
        initData();
    }

    private void initWidget()
    {
        sv_scroll = (InnerScrollView) findViewById(R.id.sv_scroll);
        tv_title = (TextView) findViewById(R.id.tv_title);
        btn_one_key_start = (Button) findViewById(R.id.btn_one_key_start);
        btn_one_key_save = (Button) findViewById(R.id.btn_one_key_save);
        btn_one_key_start.setOnClickListener(this);
        btn_one_key_save.setOnClickListener(this);

        ll_sync = (LinearLayout) findViewById(R.id.ll_sync);
        et_sync_start_date = (EditText) findViewById(R.id.et_sync_start_date);
        et_sync_end_date = (EditText) findViewById(R.id.et_sync_end_date);
        sv_download = (InnerScrollView) findViewById(R.id.sv_download);
        tv_sync_info_display = (TextView) findViewById(R.id.tv_sync_info_display);
        pb_sync_progress = (ProgressBar) findViewById(R.id.pb_sync_progress);
        btn_sync_start = (Button) findViewById(R.id.btn_sync_start);
        btn_sync_start.setOnClickListener(this);

        ll_select = (LinearLayout) findViewById(R.id.ll_select);
        et_select_start_date = (EditText) findViewById(R.id.et_select_start_date);
        et_select_start_time = (EditText) findViewById(R.id.et_select_start_time);
        et_select_end_date = (EditText) findViewById(R.id.et_select_end_date);
        et_select_end_time = (EditText) findViewById(R.id.et_select_end_time);
        et_select_caller_account = (EditText) findViewById(R.id.et_select_caller_account);
        et_select_receiver_account = (EditText) findViewById(R.id.et_select_receiver_account);
        et_select_talk_min_second = (EditText) findViewById(R.id.et_select_talk_min_second);
        et_select_talk_max_second = (EditText) findViewById(R.id.et_select_talk_max_second);
        cb_select_android = (CheckBox) findViewById(R.id.cb_select_android);
        cb_select_ios = (CheckBox) findViewById(R.id.cb_select_ios);
        cb_select_windows = (CheckBox) findViewById(R.id.cb_select_windows);
        sv_select = (InnerScrollView) findViewById(R.id.sv_select);
        tv_select_display = (TextView) findViewById(R.id.tv_select_display);
        pb_select_progress = (ProgressBar) findViewById(R.id.pb_select_progress);
        btn_select_start = (Button) findViewById(R.id.btn_select_start);
        btn_select_start.setOnClickListener(this);

        rb_order_by_count = (RadioButton) findViewById(R.id.rb_order_by_count);
        rb_order_by_rate = (RadioButton) findViewById(R.id.rb_order_by_rate);
        rb_order_by_account = (RadioButton) findViewById(R.id.rb_order_by_account);
        sv_statis = (InnerScrollView) findViewById(R.id.sv_statis);
        tv_statis_info_display = (TextView) findViewById(R.id.tv_statis_info_display);
        pb_statis_progress = (ProgressBar) findViewById(R.id.pb_statis_progress);
        btn_statis_start = (Button) findViewById(R.id.btn_statis_start);
        btn_statis_start.setOnClickListener(this);

        et_detail_account = (EditText) findViewById(R.id.et_detail_account);
        sv_detail = (InnerScrollView) findViewById(R.id.sv_detail);
        tv_detail_info_display = (TextView) findViewById(R.id.tv_detail_info_display);
        pb_detail_progress = (ProgressBar) findViewById(R.id.pb_detail_progress);
        btn_detail_start = (Button) findViewById(R.id.btn_detail_start);
        btn_detail_start.setOnClickListener(this);

        sv_download.setParentScrollView(sv_scroll);
        sv_select.setParentScrollView(sv_scroll);
        sv_statis.setParentScrollView(sv_scroll);
        sv_detail.setParentScrollView(sv_scroll);

        sv_download.setChildDiableParentScroll(false);
        sv_select.setChildDiableParentScroll(false);
        sv_statis.setChildDiableParentScroll(false);
        sv_detail.setChildDiableParentScroll(false);

        handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                switch (msg.what)
                {
                    case MSG_HIDE_SYNC_PB:
                    {
                        if (null != syncDataThread && syncDataThread.isCanceled())
                        {
                            pb_sync_progress.setVisibility(View.INVISIBLE);
                            pb_sync_progress.setProgress(0);
                            btn_sync_start.setText("开始同步");
                            tv_sync_info_display.setText("开始启动数据同步操作\r\n" + syncInfoBuffer);
                            sv_download.fullScroll(ScrollView.FOCUS_DOWN);

                            if (isOneKeyStart)
                            {
                                selectData();
                            }
                        }
                        break;
                    }
                    case MSG_SHOW_SYNC_PB:
                    {
                        // 最好是一秒或一格, 触发更新一次, 但这太麻烦, 因而采用处理完一个HTTP请求更新一次
                        // "...".substring(0, ((int) (System.currentTimeMillis() / 1000)) % 3)
                        if (null == msg.obj)
                        {
                            tv_sync_info_display.setText("开始启动数据同步操作\r\n" + syncInfoBuffer);
                        }
                        else
                        {
                            tv_sync_info_display.setText("开始启动数据同步操作\r\n" + syncInfoBuffer + msg.obj);
                            pb_sync_progress.setProgress(msg.arg1);
                        }
                        pb_sync_progress.setVisibility(View.VISIBLE);
                        sv_download.fullScroll(ScrollView.FOCUS_DOWN);

                        break;
                    }
                    case MSG_HIDE_SELECT_PB:
                    {
                        if (null != selectDataThread && selectDataThread.isCanceled())
                        {
                            pb_select_progress.setVisibility(View.INVISIBLE);
                            pb_select_progress.setProgress(0);
                            btn_select_start.setText("开始筛选");
                            tv_select_display.setText("开始启动数据筛选操作\r\n" + selectInfoBuffer);
                            sv_select.fullScroll(View.FOCUS_DOWN);

                            if (isOneKeyStart)
                            {
                                statisData();
                            }
                        }
                        break;
                    }
                    case MSG_SHOW_SELECT_PB:
                    {
                        // 已处理条数与总记录数作为进度条, 进度一格,触发刷新一次
                        if (null == msg.obj)
                        {
                            tv_select_display.setText("开始启动数据筛选操作\r\n" + selectInfoBuffer);
                        }
                        else
                        {
                            tv_select_display.setText("开始启动数据筛选操作\r\n" + selectInfoBuffer + msg.obj);
                            pb_select_progress.setProgress(msg.arg1);
                        }
                        pb_select_progress.setVisibility(View.VISIBLE);
                        pb_select_progress.setFocusable(true);
                        sv_select.fullScroll(View.FOCUS_DOWN);

                        if (isOneKeyStart)
                        {
                            sv_scroll.scrollTo(0, MainActivity.this.dip2px(10)
                                    + MainActivity.this.dip2px(10) + tv_title.getMeasuredHeight()
                                    + MainActivity.this.dip2px(10) + ll_sync.getMeasuredHeight() + MainActivity.this.dip2px(10));
                        }
                        break;
                    }
                    case MSG_HIDE_STATIS_PB:
                    {
                        if (null != statisDataThread && statisDataThread.isCanceled())
                        {
                            pb_statis_progress.setVisibility(View.INVISIBLE);
                            pb_statis_progress.setProgress(0);
                            btn_statis_start.setText("开始统计");
                            tv_statis_info_display.setText("开始启动数据统计操作\r\n" + statisInfoBuffer);
                            sv_statis.fullScroll(ScrollView.FOCUS_DOWN);
                            
                            pb_select_progress.setFocusable(true);

                            if (isOneKeyStart)
                            {
                                detailData();
                            }
                        }
                        break;
                    }
                    case MSG_SHOW_STATIS_PB:
                    {
                        if (isOneKeyStart)
                        {
                            sv_scroll.scrollTo(0, MainActivity.this.dip2px(10)
                                    + MainActivity.this.dip2px(10) + tv_title.getMeasuredHeight()
                                    + MainActivity.this.dip2px(10) + ll_sync.getMeasuredHeight() + MainActivity.this.dip2px(10)
                                    + MainActivity.this.dip2px(15) + ll_select.getMeasuredHeight());
                        }
                        // 分阶段, 1. 统计成功率, 2. 排序; 进度一格,触发刷新一次
                        if (null == msg.obj)
                        {
                            tv_statis_info_display.setText("开始启动数据统计操作\r\n" + statisInfoBuffer);
                        }
                        else
                        {
                            tv_statis_info_display.setText("开始启动数据统计操作\r\n" + statisInfoBuffer + msg.obj);
                            pb_statis_progress.setProgress(msg.arg1);
                        }
                        pb_statis_progress.setVisibility(View.VISIBLE);
                        // pb_select_progress.setFocusable(true);
                        sv_statis.fullScroll(ScrollView.FOCUS_DOWN);
                        break;
                    }
                    case MSG_HIDE_DETAIL_PB:
                    {
                        if (null != detailThread && detailThread.isCanceled())
                        {
                            pb_detail_progress.setVisibility(View.INVISIBLE);
                            pb_detail_progress.setProgress(0);
                            btn_detail_start.setText("开始查询");
                            tv_detail_info_display.setText("开始启动详情查询操作\r\n" + detailInfoBuffer);
                            sv_detail.fullScroll(ScrollView.FOCUS_DOWN);

                            if (isOneKeyStart)
                            {
                                sv_scroll.fullScroll(ScrollView.FOCUS_DOWN);
                                Toast.makeText(MainActivity.this, "一键运行操作执行完成", Toast.LENGTH_LONG).show();
                                isOneKeyStart = false;
                            }
                        }
                        break;
                    }
                    case MSG_SHOW_DETAIL_PB:
                    {
                        if (null == msg.obj)
                        {
                            tv_detail_info_display.setText("开始启动详情查询操作\r\n" + detailInfoBuffer);
                        }
                        else
                        {
                            tv_detail_info_display.setText("开始启动详情查询操作\r\n" + detailInfoBuffer + msg.obj);
                            pb_detail_progress.setProgress(msg.arg1);
                        }
                        pb_detail_progress.setVisibility(View.VISIBLE);
                        sv_detail.fullScroll(View.FOCUS_DOWN);
                        if (isOneKeyStart)
                        {
                            sv_scroll.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                        break;
                    }
                    default:
                    {
                        break;
                    }
                }

                /*
                if (null == syncInfoBuffer || "".equals(syncInfoBuffer))
                {
                    sv_download.setChildDiableParentScroll(false);
                }
                else
                {
                    sv_download.setChildDiableParentScroll(true);
                }

                if (null == selectInfoBuffer || "".equals(selectInfoBuffer))
                {
                    sv_select.setChildDiableParentScroll(false);
                }
                else
                {
                    sv_select.setChildDiableParentScroll(true);
                }

                if (null == statisInfoBuffer || "".equals(statisInfoBuffer))
                {
                    sv_statis.setChildDiableParentScroll(false);
                }
                else
                {
                    sv_statis.setChildDiableParentScroll(true);
                }

                if (null == detailInfoBuffer || "".equals(detailInfoBuffer))
                {
                    sv_detail.setChildDiableParentScroll(false);
                }
                else
                {
                    sv_detail.setChildDiableParentScroll(true);
                }
                */
            }
        };
    }

    private void initData()
    {
        syncInfoBuffer = new StringBuffer();
        selectInfoBuffer = new StringBuffer();
        statisInfoBuffer = new StringBuffer();
        detailInfoBuffer = new StringBuffer();

        recordInfoList = new LinkedList<RecordInfo>();
        statisPrintList = new LinkedList<String>();

        dayFormat = new SimpleDateFormat("yyyy-MM-dd");
        timeFomat = new SimpleDateFormat("HH:mm:ss");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        String yesterday = dayFormat.format(calendar.getTime());
        et_sync_start_date.setText(yesterday);
        et_select_start_date.setText(yesterday);

        sv_scroll.fullScroll(View.FOCUS_UP);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        et_sync_start_date.setSelection(et_sync_start_date.getText().toString().length());
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_sync_start:
            {
                syncData();
                break;
            }
            case R.id.btn_select_start:
            {
                selectData();
                break;
            }
            case R.id.btn_statis_start:
            {
                statisData();
                break;
            }
            case R.id.btn_detail_start:
            {
                detailData();
                break;
            }
            case R.id.btn_one_key_start:
            {
                oneKeyStart();
                break;
            }
            case R.id.btn_one_key_save:
            {
                oneKeySave();
                break;
            }
            default:
            {
                break;
            }
        }
    }

    private synchronized void oneKeyStart()
    {
        if (isOneKeyStart)
        {
            Toast.makeText(this, "一键运行操作正在执行...", Toast.LENGTH_LONG).show();
            return;
        }
        isOneKeyStart = true;
        Toast.makeText(this, "一键运行操作启动了", Toast.LENGTH_LONG).show();

        syncInfoBuffer = new StringBuffer();
        selectInfoBuffer = new StringBuffer();
        statisInfoBuffer = new StringBuffer();

        tv_sync_info_display.setText("");
        tv_select_display.setText("");
        tv_statis_info_display.setText("");

        sv_scroll.fullScroll(View.FOCUS_UP);

        syncData();
    }

    private void oneKeySave()
    {
        Toast.makeText(this, "一键保存操作启动了", Toast.LENGTH_SHORT).show();

        FileOutputStream fos = null;
        try
        {
            String sdcard = null;
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
            {
                sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
            }
            if (null == sdcard)
            {
                Toast.makeText(this, "SD卡未正常挂载,无法保存文件", Toast.LENGTH_LONG).show();
                return;
            }

            String filePath = sdcard + "/VoipAnalysis";
            new File(filePath).mkdirs();
            filePath = filePath + "/result.txt";
            fos = new FileOutputStream(filePath);
            fos.write("VOIP呼叫分析结果内容\r\n\r\n\r\n".getBytes());

            fos.write("(1) 数据同步信息:\r\n".getBytes());
            fos.write(syncInfoBuffer.toString().getBytes());
            fos.write("\r\n\r\n".getBytes());

            fos.write("(2) 数据筛选信息:\r\n".getBytes());
            fos.write(selectInfoBuffer.toString().getBytes());
            fos.write("\r\n\r\n".getBytes());

            fos.write("(3) 数据统计信息:\r\n".getBytes());
            fos.write(statisInfoBuffer.toString().getBytes());
            fos.write("\r\n\r\n".getBytes());

            fos.write("(4) 详情信息:\r\n".getBytes());
            fos.write(detailInfoBuffer.toString().getBytes());
            fos.write("\r\n\r\n".getBytes());

            String time = "结果输出日期: ";
            time += new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            time += "\r\n\r\n";
            fos.write(time.getBytes());

            Toast.makeText(this, "分析结果已成功保存, 文件路径为: " + filePath, Toast.LENGTH_LONG).show();
        }
        catch (Exception e)
        {
            Toast.makeText(this, "保存失败, 异常: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        finally
        {
            if (null != fos)
            {
                try
                {
                    fos.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private void syncData()
    {
        if ("取消同步".equals(btn_sync_start.getText()))
        {
            isOneKeyStart = false;
            if (null != syncDataThread)
            {
                syncDataThread.cancel();
            }
            btn_sync_start.setText("开始同步");
            return;
        }

        btn_sync_start.setText("取消同步");
        if (!checkThread())
        {
            btn_sync_start.setText("开始同步");
            return;
        }

        // 同步数据
        /**
         检查参数
         一. 检查网络
         (1) 物理网络
         (2) 内网检查 192.168.170.87 和 192.168.170.89
         (3) 外网检查 moaportal.zte.com.cn 对应的哪个AP服务器, 实际域名对应的是88,至于转到87还是89是服务器的调度的
         获取映射的AP名称, 由http://moaportal.zte.com.cn:8080/ftpserver/ip.jsp返回的名称
         服务端执行shell获取IP地址: ifconfig | grep broadcast | awk '{print $2}'
         二. 检查输入参数
         */
        Date startDate = null;
        Date endDate = null;
        // 校验起始日期
        String startDay = et_sync_start_date.getText().toString().trim();
        if (TextUtils.isEmpty(startDay))
        {
            startDay = "2016-08-01";
        }
        try
        {
            startDate = dayFormat.parse(startDay);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (null == startDate)
        {
            Toast.makeText(this, "数据同步的起始日期格式错误,请修正后再试...", Toast.LENGTH_LONG).show();
            btn_sync_start.setText("开始同步");
            return;
        }
        et_sync_start_date.setText(startDay);
        // 校验结束日期
        String endDay = et_sync_end_date.getText().toString().trim();
        if (TextUtils.isEmpty(endDay))
        {
            endDay = dayFormat.format(new Date());
        }
        try
        {
            endDate = dayFormat.parse(endDay);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (null == startDate)
        {
            Toast.makeText(this, "数据同步的结束日期格式错误,请修正后再试...", Toast.LENGTH_LONG).show();
            btn_sync_start.setText("开始同步");
            return;
        }
        et_sync_end_date.setText(endDay);

        // 获取AP列表和同步时间区段, 发起数据同步线程
        Log.e(TAG, "startDate=" + dayFormat.format(startDate) + ", endDate=" + dayFormat.format(endDate));

        syncInfoBuffer = new StringBuffer();
        selectInfoBuffer = new StringBuffer();
        statisInfoBuffer = new StringBuffer();
        detailInfoBuffer = new StringBuffer();

        tv_sync_info_display.setText("");
        tv_select_display.setText("");
        tv_statis_info_display.setText("");
        tv_detail_info_display.setText("");

        syncDataThread = new SyncDataThread(startDate, endDate);
        syncDataThread.start();
    }

    private void selectData()
    {
        if ("取消筛选".equals(btn_select_start.getText()))
        {
            isOneKeyStart = false;
            if (null != selectDataThread)
            {
                selectDataThread.cancel();
            }
            btn_select_start.setText("开始筛选");
            return;
        }

        // 筛选数据
        btn_select_start.setText("取消筛选");
        if (!checkThread())
        {
            btn_select_start.setText("开始筛选");
            return;
        }

        // 检查起始日期
        Date startDayTmp = null;
        String startDay = et_select_start_date.getText().toString().trim();
        if (TextUtils.isEmpty(startDay))
        {
            startDay = "2016-08-01";
        }
        try
        {
            startDayTmp = dayFormat.parse(startDay);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (null == startDayTmp)
        {
            Toast.makeText(this, "数据筛选的起始日期格式错误,请修正后再试...", Toast.LENGTH_LONG).show();
            btn_select_start.setText("开始筛选");
            return;
        }
        et_select_start_date.setText(startDay);
        // 检查起始时间
        Date startTimeTmp = null;
        String startTime = et_select_start_time.getText().toString().trim();
        if (TextUtils.isEmpty(startTime))
        {
            startTime = "00:00:00";
        }
        try
        {
            startTimeTmp = timeFomat.parse(startTime);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (null == startTimeTmp)
        {
            Toast.makeText(this, "数据筛选的起始时间格式错误,请修正后再试...", Toast.LENGTH_LONG).show();
            btn_select_start.setText("开始筛选");
            return;
        }
        et_select_start_time.setText(startTime);

        // 检查结束日期
        Date endDayTmp = null;
        String endDay = et_select_end_date.getText().toString().trim();
        if (TextUtils.isEmpty(endDay))
        {
            endDay = dayFormat.format(new Date());
            ;
        }
        try
        {
            endDayTmp = dayFormat.parse(endDay);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (null == endDayTmp)
        {
            Toast.makeText(this, "数据筛选的结束日期格式错误,请修正后再试...", Toast.LENGTH_LONG).show();
            btn_select_start.setText("开始筛选");
            return;
        }
        et_select_end_date.setText(endDay);
        // 检查起始时间
        Date endTimeTmp = null;
        String endTime = et_select_end_time.getText().toString().trim();
        if (TextUtils.isEmpty(endTime))
        {
            endTime = timeFomat.format(new Date());
        }
        try
        {
            endTimeTmp = timeFomat.parse(endTime);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (null == endTimeTmp)
        {
            Toast.makeText(this, "数据筛选的结束时间格式错误,请修正后再试...", Toast.LENGTH_LONG).show();
            btn_select_start.setText("开始筛选");
            return;
        }
        et_select_end_time.setText(endTime);

        String caller = et_select_caller_account.getText().toString().trim();
        et_select_caller_account.setText(caller);
        if ("".equals(caller))
        {
            caller = null;
        }

        String receiver = et_select_receiver_account.getText().toString().trim();
        et_select_receiver_account.setText(receiver);
        if ("".equals(receiver))
        {
            receiver = null;
        }

        int minSecond = 0;
        try
        {
            minSecond = Integer.parseInt(et_select_talk_min_second.getText().toString().trim());
            if (minSecond < 0 || minSecond > Integer.MAX_VALUE)
            {
                appendToSelectInfoBuffer(getCurrTime() + "接通时长值须在[0, " + Integer.MAX_VALUE + "]范围, 重置为默认值0");
                minSecond = 0;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        et_select_talk_min_second.setText("" + minSecond);

        int maxSecond = Integer.MAX_VALUE;
        try
        {
            maxSecond = Integer.parseInt(et_select_talk_max_second.getText().toString().trim());
            if (maxSecond < 0 || maxSecond > Integer.MAX_VALUE)
            {
                maxSecond = Integer.MAX_VALUE;
                appendToSelectInfoBuffer(getCurrTime() + "接通时长值须在[0, " + Integer.MAX_VALUE + "]范围, 重置为默认值" + Integer.MAX_VALUE);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        et_select_talk_max_second.setText("" + maxSecond);

        List<String> clientList = new ArrayList<String>();
        if (cb_select_android.isChecked())
        {
            clientList.add("A");
        }
        if (cb_select_ios.isChecked())
        {
            clientList.add("I");
        }
        if (cb_select_windows.isChecked())
        {
            clientList.add("W");
        }

        SelectInfo selectInfo = new SelectInfo((startDay + startTime).replace("-", "")
                , (endDay + endTime).replace("-", "")
                , caller
                , receiver
                , minSecond
                , maxSecond
                , clientList);

        selectInfoBuffer = new StringBuffer();
        statisInfoBuffer = new StringBuffer();
        detailInfoBuffer = new StringBuffer();

        tv_select_display.setText("");
        tv_statis_info_display.setText("");
        tv_detail_info_display.setText("");

        selectDataThread = new SelectDataThread(selectInfo);
        selectDataThread.start();
    }

    private void statisData()
    {
        if ("取消统计".equals(btn_statis_start.getText()))
        {
            isOneKeyStart = false;
            if (null != statisDataThread)
            {
                statisDataThread.cancel();
            }
            btn_statis_start.setText("开始统计");
            return;
        }

        // 统计数据
        btn_statis_start.setText("取消统计");
        if (!checkThread())
        {
            btn_statis_start.setText("开始统计");
            return;
        }

        if (isSyncDataChanged)
        {
            Toast.makeText(this, "同步的数据已发生了变化, 请先执行筛选操作后再做数据统计", Toast.LENGTH_LONG).show();
            btn_statis_start.setText("开始统计");
            return;
        }

        if (rb_order_by_count.isChecked())
        {
            orderType = 0;
        }
        else if (rb_order_by_rate.isChecked())
        {
            orderType = 1;
        }
        else if (rb_order_by_account.isChecked())
        {
            orderType = 2;
        }

        statisInfoBuffer = new StringBuffer();
        detailInfoBuffer = new StringBuffer();

        tv_statis_info_display.setText("");
        tv_detail_info_display.setText("");

        statisDataThread = new StatisDataThread();
        statisDataThread.start();
    }

    private void detailData()
    {
        if ("取消查询".equals(btn_detail_start))
        {
            isOneKeyStart = false;
            if (null != detailThread)
            {
                detailThread.cancel();
            }
            btn_detail_start.setText("开始查询");
            return;
        }

        // 查询详情
        btn_detail_start.setText("取消查询");
        if (!checkThread())
        {
            btn_detail_start.setText("开始查询");
            return;
        }

        if (null == map && map.size() <= 0)
        {
            Toast.makeText(this, "统计数据内容为空, 请重新做数据统计后再查询详情", Toast.LENGTH_LONG).show();
            btn_detail_start.setText("开始查询");
            return;
        }

        if (isSelectDataChanged)
        {
            Toast.makeText(this, "统计数据已发生了变化, 请数据统计操作后再查询详情", Toast.LENGTH_LONG).show();
            btn_detail_start.setText("开始查询");
            return;
        }

        detailInfoBuffer = new StringBuffer();

        tv_detail_info_display.setText("");

        detailThread = new DetailThread(et_detail_account.getText().toString());
        detailThread.start();
    }

    private boolean checkThread()
    {
        boolean isOk = false;

        if (null != statisDataThread && !statisDataThread.isCanceled())
        {
            Toast.makeText(this, "正在统计数据,请取消操作后再试...", Toast.LENGTH_LONG).show();
        }
        else if (null != syncDataThread && !syncDataThread.isCanceled())
        {
            Toast.makeText(this, "正在同步数据,请取消操作后再试...", Toast.LENGTH_LONG).show();
        }
        else if (null != selectDataThread && !selectDataThread.isCanceled())
        {
            Toast.makeText(this, "正在筛选数据,请取消操作后再试...", Toast.LENGTH_LONG).show();
        }
        else if (null != detailThread && !detailThread.isCanceled())
        {
            Toast.makeText(this, "正在查询详情数据,请取消操作后再试...", Toast.LENGTH_LONG).show();
        }
        else
        {
            isOk = true;
        }

        return isOk;
    }

    private void appendToSyncInfoBuffer(String content)
    {
        syncInfoBuffer.append("\r\n" + content);
        /*
        if (null != handler && !handler.hasMessages(MSG_SHOW_SYNC_PB))
        {
            handler.sendEmptyMessage(MSG_SHOW_SYNC_PB);
        }
        */
    }

    private void appendToSelectInfoBuffer(String content)
    {
        selectInfoBuffer.append("\r\n" + content);
        /*
        if (null != handler && !handler.hasMessages(MSG_SHOW_SELECT_PB))
        {
            handler.sendEmptyMessage(MSG_SHOW_SELECT_PB);
        }
        */
    }

    private void appendToStatisInfoBuffer(String content)
    {
        statisInfoBuffer.append("\r\n" + content);
        /*
        if (null != handler && !handler.hasMessages(MSG_SHOW_STATIS_PB))
        {
            handler.sendEmptyMessage(MSG_SHOW_STATIS_PB);
        }
        */
    }

    private void appendToDetailInfoBuffer(String content)
    {
        detailInfoBuffer.append("\r\n" + content);
        /*
        if (null != handler && !handler.hasMessages(MSG_SHOW_DETAIL_PB))
        {
            handler.sendEmptyMessage(MSG_SHOW_DETAIL_PB);
        }
        */
    }

    private void updateProgress(int msgType, int progress, String hint)
    {
        if (null != handler)
        {
            if (handler.hasMessages(msgType))
            {
                handler.removeMessages(msgType);
            }

            Message msg = Message.obtain();
            msg.what = msgType;
            msg.arg1 = progress;
            msg.obj = "\r\n" + hint;

            handler.sendMessage(msg);
        }
    }

    private String getServerName(String server)
    {
        // http://moaportal.zte.com.cn:8080/ftpserver/ip.jsp返回的名称
        String serverName = null;

        HttpURLConnection conn = null;
        BufferedReader br = null;
        InputStream is = null;
        try
        {
            URL url = new URL("http://" + server + ":8080/ftpserver/ip.jsp");
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.setReadTimeout(10 * 1000);
            setHeader(conn, "http://" + server + ":8080/ftpserver/ip.jsp");
            conn.setRequestProperty("Content-Type", "text/html");
            conn.connect();
            // br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            // serverName = br.readLine();
            is = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int len = is.read(buffer);
            serverName = new String(buffer, 0, len);
            serverName = serverName.trim();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (null != is)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (null != br)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (null != conn)
            {
                conn.disconnect();
            }
        }

        Log.i(TAG, "server=" + server + ", serverName=" + serverName);
        return serverName;
    }

    /**
     * 设置URLConnection的头部信息，伪装请求信息
     */
    public void setHeader(HttpURLConnection conn, String downloadUrl)
    {
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.3) Gecko/2008092510 Ubuntu/8.04 (hardy) Firefox/3.0.3");
        conn.setRequestProperty("Accept-Language", "en-us,en;q=0.7,zh-cn;q=0.3");
        conn.setRequestProperty("Accept-Encoding", "utf-8");
        conn.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        conn.setRequestProperty("Keep-Alive", "300");
        conn.setRequestProperty("connnection", "keep-alive");
        conn.setRequestProperty("If-Modified-Since", "Fri, 02 Jan 2015 17:00:05 GMT");
        conn.setRequestProperty("If-None-Match", "\"1261d8-4290-df64d224\"");
        conn.setRequestProperty("Cache-conntrol", "max-age=0");
        conn.setRequestProperty("Referer", downloadUrl);
    }

    private String getCurrTime()
    {
        return timeFomat.format(new Date()) + " ";
    }

    public int dip2px(float dipValue)
    {
        return (int) (dipValue * getResources().getDisplayMetrics().density + 0.5f);
    }

    private class SyncDataThread extends Thread
    {
        private Map<String, String> serverMap;
        private Date startDate;
        private Date endDate;

        private boolean isCanceled;

        private SimpleDateFormat monthFormat = new SimpleDateFormat("yyyyMM");
        private SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");

        public SyncDataThread(Date startDate, Date endDate)
        {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public boolean isCanceled()
        {
            return isCanceled;
        }

        public synchronized void cancel()
        {
            if (isCanceled)
            {
                return;
            }
            isCanceled = true;

            if (null != handler)
            {
                handler.sendEmptyMessageDelayed(MSG_HIDE_SYNC_PB, 2 * 1000);
            }
        }

        @Override
        public void run()
        {
            syncInfoBuffer = new StringBuffer();

            // 校验AP
            serverMap = new HashMap<String, String>();

            String time = getCurrTime();
            updateProgress(MSG_SHOW_SYNC_PB, 0, time + "开始检查服务器(192.168.170.87)...");
            String serverName = getServerName("192.168.170.87");
            appendToSyncInfoBuffer(time + "开始检查服务器(192.168.170.87)...");
            time = getCurrTime();
            if ("192.168.170.87".equals(serverName))
            {
                serverMap.put("192.168.170.87", "192.168.170.87");
                updateProgress(MSG_SHOW_SYNC_PB, 5, time + "检查服务器(192.168.170.87) OK");
                appendToSyncInfoBuffer(time + "检查服务器(192.168.170.87) OK");
            }
            else
            {
                updateProgress(MSG_SHOW_SYNC_PB, 5, time + "检查服务器(192.168.170.87) ERROR");
                appendToSyncInfoBuffer(time + "检查服务器(192.168.170.87) ERROR");
            }

            time = getCurrTime();
            updateProgress(MSG_SHOW_SYNC_PB, 10, time + "开始检查服务器(192.168.170.89)...");
            serverName = getServerName("192.168.170.89");
            appendToSyncInfoBuffer(time + "开始检查服务器(192.168.170.89)...");
            time = getCurrTime();
            if ("192.168.170.89".equals(serverName))
            {
                serverMap.put("192.168.170.89", "192.168.170.89");
                updateProgress(MSG_SHOW_SYNC_PB, 10, time + "检查服务器(192.168.170.89) OK");
                appendToSyncInfoBuffer(time + "检查服务器(192.168.170.89) OK");
            }
            else
            {
                updateProgress(MSG_SHOW_SYNC_PB, 10, time + "检查服务器(192.168.170.89) ERROR");
                appendToSyncInfoBuffer(time + "检查服务器(192.168.170.89) ERROR");
            }

            if (serverMap.size() <= 0)
            {
                // 可能是外网
                time = getCurrTime();
                updateProgress(MSG_SHOW_SYNC_PB, 15, time + "开始检查服务器(moaportal.zte.com.cn)...");
                String ip = getServerName("moaportal.zte.com.cn");
                appendToSyncInfoBuffer(time + "开始检查服务器(moaportal.zte.com.cn)...");
                time = getCurrTime();
                if ("192.168.170.87".equals(ip) || "192.168.170.89".equals(ip))
                {
                    serverMap.put("moaportal.zte.com.cn", ip);
                    updateProgress(MSG_SHOW_SYNC_PB, 15, time + "检查服务器(moaportal.zte.com.cn) OK, 映射为 " + ip);
                    appendToSyncInfoBuffer(time + "检查服务器(moaportal.zte.com.cn) OK, 映射为 " + ip);
                }
                else
                {
                    updateProgress(MSG_SHOW_SYNC_PB, 15, time + "检查服务器(moaportal.zte.com.cn) ERROR");
                    appendToSyncInfoBuffer(time + "检查服务器(moaportal.zte.com.cn) ERROR");
                    updateProgress(MSG_SHOW_SYNC_PB, 20, getCurrTime() + "无法连接到服务器,同步失败");
                }
            }

            if (check())
            {
                updateProgress(MSG_SHOW_SYNC_PB, 20, getCurrTime() + "校验结果 OK");
                download();
            }

            appendToSyncInfoBuffer(getCurrTime() + "本次同步操作结束");
            isSyncDataChanged = true;

            // 结束修改标识
            cancel();
        }

        private boolean check()
        {
            if (null == serverMap && serverMap.size() <= 0)
            {
                appendToSyncInfoBuffer(getCurrTime() + "serverMap is empty.");
                return false;
            }

            if (null == startDate)
            {
                appendToSyncInfoBuffer(getCurrTime() + "startDate is null.");
                return false;
            }

            if (null == endDate)
            {
                appendToSyncInfoBuffer(getCurrTime() + "endDate is null.");
                return false;
            }

            return true;
        }

        private void download()
        {
            // 本地SD卡路径, 没SD卡或只读则无法下载
            String sdcard = null;
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
            {
                sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
            }
            if (null == sdcard)
            {
                appendToSyncInfoBuffer(getCurrTime() + " SD卡未正常挂载,取消数据同步操作");
                return;
            }

            // 交替从各AP下载记录信息
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(startDate);
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(endDate);

            int currNum = 0;
            int totalNum = (int) (serverMap.size() * (endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis()) / 1000 / 60 / 60 / 24);
            int progress = 0;

            Log.i(TAG, "startDate=" + startDate + ", endDate=" + endDate + ", totalNum=" + totalNum);

            Set<String> serverList = serverMap.keySet();
            while (startCalendar.compareTo(endCalendar) < 0)
            {
                if (isCanceled)
                {
                    break;
                }

                for (String server : serverList)
                {
                    if (isCanceled)
                    {
                        appendToSyncInfoBuffer(getCurrTime() + "用户取消了数据同步操作");
                        break;
                    }

                    // http://moaportal.zte.com.cn:8080/ftpserver/VoipRecord/201608/moavoip_cdr.20160811.txt
                    // /sdcard/VoipAnalysis/VoipRecord/192.168.170.87/201608/moavoip_cdr.20160811.txt
                    String month = monthFormat.format(startCalendar.getTime());
                    String day = dayFormat.format(startCalendar.getTime());
                    String fileName = "moavoip_cdr." + day + ".txt";
                    String downloadUrl = "http://" + server + ":8080/ftpserver/VoipRecord/"
                            + month + "/"
                            + fileName;
                    String saveFilePath = sdcard + "/VoipAnalysis/VoipRecord/" + serverMap.get(server) + "/"
                            + month + "/"
                            + fileName;
                    File dirFile = new File(saveFilePath.substring(0, saveFilePath.lastIndexOf("/")));
                    if (!dirFile.isDirectory())
                    {
                        dirFile.mkdirs();
                    }

                    currNum++;
                    if (20 + currNum * 80 / totalNum != progress)
                    {
                        progress = 20 + currNum * 80 / totalNum;
                        updateProgress(MSG_SHOW_SYNC_PB, progress, "服务器(" + server + ")的文件 " + fileName + " 正在同步... ");
                    }

                    int resultCode = httpReq(downloadUrl, saveFilePath);
                    switch (resultCode)
                    {
                        case 0:
                        {
                            appendToSyncInfoBuffer(getCurrTime() + fileName + " 从服务器(" + server + ")同步到本地成功");
                            break;
                        }
                        case 1:
                        {
                            appendToSyncInfoBuffer(getCurrTime() + fileName + " 本地文件已经是最新的");
                            break;
                        }
                        case 2:
                        {
                            appendToSyncInfoBuffer(getCurrTime() + fileName + " 文件内容为空, 无需要同步");
                            break;
                        }
                        case 3:
                        {
                            appendToSyncInfoBuffer(getCurrTime() + fileName + " 请求文件不存在, 无需要同步");
                            break;
                        }
                        default:
                        {
                            appendToSyncInfoBuffer(getCurrTime() + fileName + " 从服务器(" + server + ")同步到本地失败");
                            break;
                        }
                    }
                }

                startCalendar.add(Calendar.DATE, 1);
            }
        }

        /**
         * @param downloadUrl
         * @param saveFilePath
         * @return 结果码
         * 0-下载成功
         * 1-本地已经下载过了, 文件名称和大小与服务器上的相同, 不重复下载
         * 999-未知异常
         */
        private int httpReq(String downloadUrl, String saveFilePath)
        {
            // 查询要下载文件的大小
            HttpURLConnection conn = null;
            FileOutputStream fos = null;
            try
            {
                URL url = new URL(downloadUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10 * 1000);
                conn.setReadTimeout(60 * 1000);
                setHeader(conn, downloadUrl); // 设置消息头
                conn.connect();
                int respnsecode = conn.getResponseCode();
                if (200 != respnsecode)
                {
                    Log.d(TAG, "downloadUrl=" + downloadUrl + ", respnsecode=" + respnsecode);
                    return 3;
                }
                int fileTotalLen = conn.getContentLength(); // 记录文件的总长度
                if (fileTotalLen <= 0)
                {
                    Log.d(TAG, "downloadUrl=" + downloadUrl + ", fileTotalLen=" + fileTotalLen);
                    return 2;
                }

                // 域名moaportal.zte.com.cn对应的IP是192.168.170.88这个服务器,由88根据负载均衡判断转给87或89
                // 但是先前访问到的ip.jsp获得IP, 具有一定可参考性
                long localFileLen2 = 0;
                if (-1 != downloadUrl.indexOf("moaportal.zte.com.cn"))
                {
                    String saveFilePath2 = "";
                    if (-1 != saveFilePath.indexOf("192.168.170.87"))
                    {
                        saveFilePath2.replace("192.168.170.87", "192.168.170.89");
                    }
                    else
                    {
                        saveFilePath2 = saveFilePath.replace("192.168.170.89", "192.168.170.87");
                    }

                    File logcaFile2 = new File(saveFilePath2);
                    if (logcaFile2.isFile())
                    {
                        localFileLen2 = logcaFile2.length();
                        if ( localFileLen2  == fileTotalLen)
                        {
                            Log.i(TAG, "file lenth is the same, downloadUrl=" + downloadUrl + ", saveFilePath2=" + saveFilePath2);
                        }
                        return 1;
                    }
                }

                File localFile = new File(saveFilePath);
                /*
                FileInputStream fis = new FileInputStream(localFile);
                int localStreamLen = fis.available();
                Log.d(TAG, "downloadUrl=" + downloadUrl
                        + ", fileTotalLen=" + fileTotalLen
                        + ", localPath=" + saveFilePath
                        + ", localLen=" + localFile.length()
                        + ", localStreamLen=" + localStreamLen);
                        */
                long localFileLen = 0;
                if (localFile.isFile())
                {
                    localFileLen = localFile.length();
                    if (localFileLen == fileTotalLen)
                    {
                        Log.i(TAG, "file lenth is the same, downloadUrl=" + downloadUrl + ", saveFilePath=" + saveFilePath);
                        return 1;
                    }
                }

                Log.i(TAG, "fileTotalLen=" + fileTotalLen
                        + ", localFileLen=" + localFileLen
                        + ", localFileLen2=" + localFileLen2
                        + ", downloadUrl=" + downloadUrl
                        + ", saveFilePath=" + saveFilePath);

                fos = new FileOutputStream(localFile);
                InputStream is = conn.getInputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while (-1 != (len = is.read(buffer)))
                {
                    fos.write(buffer, 0, len);
                }

                return 0;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (null != fos)
                {
                    try
                    {
                        fos.close();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                if (null != conn)
                {
                    conn.disconnect();
                }
            }

            return 999;
        }
    }

    private class SelectDataThread extends Thread
    {
        private SelectInfo selectInfo;
        private List<RecordInfo> records;
        private boolean isCanceled;

        public SelectDataThread(SelectInfo selectInfo)
        {
            this.selectInfo = selectInfo;
            this.records = new LinkedList<RecordInfo>();
        }

        public boolean isCanceled()
        {
            return isCanceled;
        }

        public synchronized void cancel()
        {
            if (isCanceled)
            {
                return;
            }
            isCanceled = true;

            if (null != handler)
            {
                handler.sendEmptyMessageDelayed(MSG_HIDE_SELECT_PB, 2 * 1000);
            }
        }

        @Override
        public void run()
        {
            selectInfoBuffer = new StringBuffer();

            if (check())
            {
                appendToSelectInfoBuffer(getCurrTime() + "参数检验: OK");
                appendToSelectInfoBuffer(getCurrTime() + "开始搜索匹配的文件路径列表");
                List<String> logFilePathList = getFilePathList();
                if (null == logFilePathList|| logFilePathList.size() <= 0)
                {
                    appendToSelectInfoBuffer(getCurrTime() + "搜索文件路径列表为空");
                }
                else
                {
                    int currNum = 0;
                    int totalNum = logFilePathList.size();
                    int progress = 0;

                    for (String logFilePath : logFilePathList)
                    {
                        if (isCanceled)
                        {
                            break;
                        }

                        appendToSelectInfoBuffer(getCurrTime() + "读取记录文件 " + logFilePath);
                        currNum++;
                        if (currNum * 100 / totalNum != progress)
                        {
                            progress = currNum * 100 / totalNum;
                            updateProgress(MSG_SHOW_SELECT_PB, progress, "正在筛选...");
                        }

                        readFromFile(logFilePath);
                    }
                }
            }

            appendToSelectInfoBuffer(getCurrTime() + "筛选完成, 匹配的记录总条数为: " + records.size());
            recordInfoList = records;
            isSyncDataChanged = false;
            isSelectDataChanged = true;
            cancel();
        }

        private boolean check()
        {
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
            {
                appendToSelectInfoBuffer(getCurrTime() + "SD卡未正确挂断, 无法获取数据");
                return false;
            }

            return true;
        }

        private List<String> getFilePathList()
        {
            // /sdcard/VoipAnalysis/VoipRecord/192.168.170.87/201608/moavoip_cdr.20160811.txt
            List<String> list = new LinkedList<String>();

            String startMonth = selectInfo.getStartDate().substring(0, 6);
            String endMonth = selectInfo.getEndDate().substring(0, 6);
            Log.i(TAG, "getFilePathList startMonth=" + startMonth + ", endMonth=" + endMonth);

            String startLogName = "moavoip_cdr." + selectInfo.getStartDate().substring(0, 8) + ".txt";
            String endLogName = "moavoip_cdr." + selectInfo.getEndDate().substring(0, 8) + ".txt";
            Log.i(TAG, "getFilePathList startLogName=" + startLogName + ", endLogName=" + endLogName);

            String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/VoipAnalysis/VoipRecord";
            Log.i(TAG, "rootPath=" + rootPath);
            File rootFile = new File(rootPath);
            if (!rootFile.isDirectory())
            {
                return list;
            }

            String[] servers = rootFile.list();
            if (null == servers || servers.length <= 0)
            {
                Log.d(TAG, "servers is empty");
                return list;
            }

            for (String server : servers)
            {
                String serverPath = rootPath + "/" + server;
                Log.i(TAG, "serverPath=" + serverPath);
                File serverFile = new File(serverPath);
                if (!serverFile.isDirectory())
                {
                    continue;
                }
                String[] months = serverFile.list();
                if (null == months || months.length <= 0)
                {
                    Log.i(TAG, "months is empty");
                    continue;
                }

                for (String month : months)
                {
                    Log.i(TAG, "month=" + month);
                    if (month.compareTo(startMonth) < 0 || month.compareTo(endMonth) > 0)
                    {
                        continue;
                    }

                    String monthPath = serverPath + "/" + month;
                    Log.i(TAG, "monthPath=" + monthPath);
                    File monthFile = new File(monthPath);
                    if (!monthFile.isDirectory())
                    {
                        continue;
                    }
                    String[] logNames = monthFile.list();
                    if (null == logNames || logNames.length <= 0)
                    {
                        Log.i(TAG, "logNames is empty");
                        continue;
                    }

                    for (String logName : logNames)
                    {
                        Log.i(TAG, "logName=" + logName);
                        if (logName.compareTo(startLogName) < 0 || logName.compareTo(endLogName) > 0)
                        {
                            continue;
                        }

                        String logPath = monthPath + "/" + logName;
                        File logFile = new File(logPath);
                        if (logFile.isFile())
                        {
                            list.add(logPath); // 最终找到符合条件的日志路径
                            Log.d(TAG, "匹配的文件路径: " + logPath);
                            // appendToSelectInfoBuffer(getCurrTime() + "匹配的文件路径: " + logPath);
                            Log.i(TAG, "match logPath=" + logPath);
                        }
                        else
                        {
                            Log.i(TAG, "not match logPath=" + logPath);
                        }
                    }
                }
            }

            appendToSelectInfoBuffer(getCurrTime() + "匹配的文件路径个数为: " + list.size());
            return list;
        }

        private void readFromFile(String logFilePath)
        {
            Log.i(TAG, "readFromFile logFilePath=" + logFilePath);

            int num = 0;
            BufferedReader br = null;
            try
            {
                br = new BufferedReader(new FileReader(logFilePath));
                String oneline = null;
                while (null != (oneline = br.readLine()))
                {
                    Log.d(TAG, "oneline=" + oneline);
                    if ("".equals(oneline))
                    {
                        continue;
                    }

                    RecordInfo info = new RecordInfo(oneline);
                    // 根据筛选条件判断
                    if (info.getTime().compareTo(selectInfo.getStartDate()) < 0)
                    {
                        Log.d(TAG, "smaller than startDate, continue");
                        continue;
                    }

                    if (info.getTime().compareTo(selectInfo.getEndDate()) >= 0)
                    {
                        Log.d(TAG, "bigger than EndDate, continue");
                        continue;
                    }

                    if (!selectInfo.getClientTypeList().contains(info.getType()))
                    {
                        Log.d(TAG, "not contain type, continue");
                        continue;
                    }

                    if (null != selectInfo.getCaller()
                            && -1 == info.getCallerUri().indexOf(selectInfo.getCaller()))
                    {
                        Log.d(TAG, "not fit caller, continue");
                        continue;
                    }

                    if (null != selectInfo.getReceiver()
                            && -1 == info.getReceiverUri().indexOf(selectInfo.getReceiver()))
                    {
                        Log.d(TAG, "not fit caller, continue");
                        continue;
                    }

                    if (info.getDuration() < selectInfo.getMinConnectedTime())
                    {
                        Log.d(TAG, "smaller than min duration, continue");
                        continue;
                    }

                    if (info.getDuration() >= selectInfo.getMaxConnectedTime())
                    {
                        Log.d(TAG, "bigger than max duration");
                        continue;
                    }

                    /*
                    int size = records.size();
                    int pos = 0;
                    for (int i=0; i<size; i++)
                    {
                        if (info.getTime().compareTo(records.get(i).getTime()) >= 0)
                        {
                            pos = i;
                            break;
                        }
                    }
                    records.add(pos, info);
                    */

                    records.add(info);
                    Log.d(TAG, "匹配的记录: " + oneline);
                    num++;
                }
                appendToSelectInfoBuffer(getCurrTime() + "匹配的记录新增条数: " + num);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (null != br)
                {
                    try
                    {
                        br.close();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class StatisDataThread extends Thread
    {
        private boolean isCanceled;

        public boolean isCanceled()
        {
            return isCanceled;
        }

        public synchronized void cancel()
        {
            if (isCanceled)
            {
                return;
            }
            isCanceled = true;

            if (null != handler)
            {
                handler.sendEmptyMessageDelayed(MSG_HIDE_STATIS_PB, 2 * 1000);
            }
        }

        @Override
        public void run()
        {
            statisInfoBuffer = new StringBuffer();

            if (check())
            {
                appendToStatisInfoBuffer(getCurrTime() + "参数信息校验: OK");
                updateProgress(MSG_SHOW_STATIS_PB, 0, "开始统计...");

                // 筛选结果中的所有呼叫者的的总体统计
                statisAllCallerResult();

                // 筛选结果中的各个呼叫者的统计
                map = statisSingleCallerResult();

                // 以主叫URI和呼通率排序
                List<StatisInfo> statisInfoList = orderStatisResult(map);

                // 格式化输出列表
                List<String> detailStatisResult = getDetailStatisResult(statisInfoList);

                if (null != detailStatisResult)
                {
                    statisPrintList = detailStatisResult;
                    StringBuffer sb = new StringBuffer("\r\n\r\n");
                    for (String record : statisPrintList)
                    {
                        Log.d(TAG, "record=" + record);
                        sb.append(record + "\r\n");
                    }
                    appendToStatisInfoBuffer(sb.toString());
                }
            }

            // 正常结束了
            updateProgress(MSG_SHOW_STATIS_PB, 100, "");
            isSelectDataChanged = false;
            cancel();
        }

        /**
         * 对筛选结果的所有呼叫者做总体的统计
         */
        private void statisAllCallerResult()
        {
            updateProgress(MSG_SHOW_STATIS_PB, 0, "筛选结果的总体的统计中...");
            String time = getCurrTime();

            if (isCanceled)
            {
                return;
            }
            if (null == recordInfoList)
            {
                Log.e(TAG, "statisAllCallerResult recordInfoList is null, so return");
                return;
            }

            StatisInfo statisInfo = new StatisInfo();

            int totalNum = recordInfoList.size();
            int currNum = 0;
            int progress = 0;

            for (RecordInfo recordInfo : recordInfoList)
            {
                if (isCanceled)
                {
                    return;
                }
                statisInfo.add(recordInfo.getDuration(), recordInfo.getResultCode());

                currNum++;
                if (0 + 30 * currNum / totalNum != progress)
                {
                    progress = 0 + 30 * currNum / totalNum;
                    updateProgress(MSG_SHOW_STATIS_PB
                            , progress
                            , "筛选结果的总体的统计" + "...".substring(currNum % 3));
                }
            }

            appendToStatisInfoBuffer(time + "筛选结果的总体的统计中...");
            appendToStatisInfoBuffer(getCurrTime() + "筛选数据的总体统计结果: " + "\r\n"
                    + "总呼叫次数: " + statisInfo.getTotalNum() + "\r\n"
                    + "正常通话次数: " + statisInfo.getConnectedNum() + "\r\n"
                    + "对方忙碌次数: " + statisInfo.getBusyNum() + "\r\n"
                    + "对方拒绝次数: " + statisInfo.getRefusedNum() + "\r\n"
                    + "呼通率: " + statisInfo.getSuccessRate() + "%\r\n\r\n");
        }

        /**
         * 对筛选结果中呼叫者各自统计
         *
         * @return map
         */
        private Map<String, List<RecordInfo>> statisSingleCallerResult()
        {
            updateProgress(MSG_SHOW_STATIS_PB, 30, "以主叫URI分类统计中...");
            String time = getCurrTime();

            if (null == recordInfoList)
            {
                Log.e(TAG, "statisSingleCallerResult recordInfoList is null, so return null");
                return null;
            }

            Map<String, List<RecordInfo>> map = new HashMap<String, List<RecordInfo>>();

            int totalNum = recordInfoList.size();
            int currNum = 0;
            int progress = 0;

            for (RecordInfo recordInfo : recordInfoList)
            {
                if (isCanceled)
                {
                    return null;
                }

                List<RecordInfo> list = map.get(recordInfo.getCallerUri());
                if (null == list)
                {
                    list = new LinkedList<RecordInfo>();
                    map.put(recordInfo.getCallerUri(), list);
                }

                list.add(recordInfo);

                currNum++;
                if (30 + 30 * currNum / totalNum != progress)
                {
                    progress = 30 + 30 * currNum / totalNum;
                    updateProgress(MSG_SHOW_STATIS_PB
                            , progress
                            , "以主叫URI分类统计中" + "...".substring(currNum % 3));
                }

                // 20160811100345|A|sip:10039081@zte.com.cn|sip:10193455@zte.com.cn|28|0|CallEnd_Success
                    /*
                    int pos = 0;
                    for (int i=0; i<list.size(); i++)
                    {
                        if (recordInfo.getTime().compareTo(list.get(i).getTime()) >= 0)
                        {
                            pos = i;
                            break;
                        }
                    }
                    list.add(pos, recordInfo);
                    */

                    /*
                    list.add(recordInfo.getTime()
                            + "|" + recordInfo.getType()
                            + "|" + recordInfo.getCallerUri()
                            + "|" + recordInfo.getReceiverUri()
                            + "|" + recordInfo.getDuration()
                            + "|" + recordInfo.getResultCode()
                            + "|" + recordInfo.getResultDesc()
                            + "|" + recordInfo.getRinged());
                            */
            }

            appendToStatisInfoBuffer(time + "以主叫URI分类统计中...");
            appendToStatisInfoBuffer(getCurrTime() + "以呼叫者URI分类完成, 主叫个数: " + map.size());

            return map;
        }

        /**
         * 对每个主叫进行统计, 按呼叫次数, 接通率进行排序
         *
         * @param map
         * @return
         */
        private List<StatisInfo> orderStatisResult(Map<String, List<RecordInfo>> map)
        {
            updateProgress(MSG_SHOW_STATIS_PB, 60, "以呼叫次数和接通率排序中...");
            String time = getCurrTime();

            if (null == map)
            {
                Log.i(TAG, "orderStatisResult map is null, so return null");
                return null;
            }

            List<StatisInfo> statisInfoList = new LinkedList<StatisInfo>();
            Set<String> set = map.keySet();
            if (null == set || set.size() <= 0)
            {
                Log.i(TAG, "orderStatisResult set is empty");
            }
            else
            {
                int totalNum = set.size();
                int currNum = 0;
                int progress = 0;

                for (String key : set)
                {
                    if (isCanceled)
                    {
                        return null;
                    }

                    List<RecordInfo> list = map.get(key);
                    if (null == list || list.size() <= 0)
                    {
                        Log.d(TAG, "key=" + key + ", list is empty");
                    }
                    else
                    {
                        Log.d(TAG, "key=" + key + ", list size is " + list.size());

                        StatisInfo statisInfoTmp = new StatisInfo();
                        statisInfoTmp.setCallerUri(key);
                        for (RecordInfo recordInfoTmp : list)
                        {
                            statisInfoTmp.add(recordInfoTmp.getDuration(), recordInfoTmp.getResultCode());
                        }

                        int pos = 0;
                        boolean isCompared = false;
                        for (int i = 0; i < statisInfoList.size(); i++)
                        {
                            if (0 == orderType)
                            {
                                if (statisInfoTmp.getTotalNum() < statisInfoList.get(i).getTotalNum())
                                {
                                    continue;
                                }

                                if (statisInfoTmp.getTotalNum() == statisInfoList.get(i).getTotalNum())
                                {
                                    if (statisInfoTmp.getSuccessRate() < statisInfoList.get(i).getSuccessRate())
                                    {
                                        continue;
                                    }

                                    if (statisInfoTmp.getSuccessRate() == statisInfoList.get(i).getSuccessRate())
                                    {

                                        if (statisInfoTmp.getCallerUri().length() > statisInfoList.get(i).getCallerUri().length())
                                        {
                                            continue;
                                        }

                                        if (statisInfoTmp.getCallerUri().length() == statisInfoList.get(i).getCallerUri().length()
                                                && statisInfoTmp.getCallerUri().compareTo(statisInfoList.get(i).getCallerUri()) > 0)
                                        {
                                            continue;
                                        }
                                    }
                                }
                            }
                            else if (1 == orderType)
                            {
                                if (statisInfoTmp.getSuccessRate() < statisInfoList.get(i).getSuccessRate())
                                {
                                    continue;
                                }

                                if (statisInfoTmp.getSuccessRate() == statisInfoList.get(i).getSuccessRate())
                                {
                                    if (statisInfoTmp.getTotalNum() < statisInfoList.get(i).getTotalNum())
                                    {
                                        continue;
                                    }

                                    if (statisInfoTmp.getTotalNum() > statisInfoList.get(i).getTotalNum())
                                    {
                                        if (statisInfoTmp.getCallerUri().length() > statisInfoList.get(i).getCallerUri().length())
                                        {
                                            continue;
                                        }

                                        if (statisInfoTmp.getCallerUri().length() == statisInfoList.get(i).getCallerUri().length()
                                                && statisInfoTmp.getCallerUri().compareTo(statisInfoList.get(i).getCallerUri()) > 0)
                                        {
                                            continue;
                                        }
                                    }
                                }
                            }
                            else if (2 == orderType)
                            {
                                if (statisInfoTmp.getCallerUri().length() > statisInfoList.get(i).getCallerUri().length()
                                        || (statisInfoTmp.getCallerUri().length() == statisInfoList.get(i).getCallerUri().length()
                                        && statisInfoTmp.getCallerUri().compareTo(statisInfoList.get(i).getCallerUri()) > 0))
                                {
                                    continue;
                                }

                                if (statisInfoTmp.getCallerUri().length() == statisInfoList.get(i).getCallerUri().length()
                                        && statisInfoTmp.getCallerUri().compareTo(statisInfoList.get(i).getCallerUri()) == 0)
                                {
                                    if (statisInfoTmp.getTotalNum() < statisInfoList.get(i).getTotalNum())
                                    {
                                        continue;
                                    }

                                    if (statisInfoTmp.getTotalNum() == statisInfoList.get(i).getTotalNum())
                                    {
                                        if (statisInfoTmp.getSuccessRate() < statisInfoList.get(i).getSuccessRate())
                                        {
                                            continue;
                                        }
                                    }
                                }
                            }

                            pos = i;
                            isCompared = true;
                            Log.d(TAG, "pos=" + pos + ", currUri=" + statisInfoTmp.getCallerUri() + ", comparedUri=" + statisInfoList.get(i).getCallerUri());
                            break;
                        }

                        if (isCompared)
                        {
                            statisInfoList.add(pos, statisInfoTmp);
                        }
                        else
                        {
                            statisInfoList.add(statisInfoTmp);
                        }

                        Log.i(TAG, "statisInfoList add, pos=" + pos + ", statisInfoTmp=" + statisInfoTmp);
                    }

                    currNum++;
                    if (60 + 30 * currNum / totalNum != progress)
                    {
                        progress = 60 + 30 * currNum / totalNum;
                        updateProgress(MSG_SHOW_STATIS_PB
                                , progress
                                , "以呼叫次数和接通率排序中" + "...".substring(currNum % 3));
                    }
                }
            }

            appendToStatisInfoBuffer(time + "以呼叫次数和接通率排序中...");
            appendToStatisInfoBuffer(getCurrTime() + "排序完成");

            return statisInfoList;
        }

        /**
         * 将排序好的列表格式化输出
         *
         * @param statisInfoList
         * @return
         */
        private List<String> getDetailStatisResult(List<StatisInfo> statisInfoList)
        {
            updateProgress(MSG_SHOW_STATIS_PB, 90, "输出结果格式化中...");
            String time = getCurrTime();

            if (null == statisInfoList)
            {
                Log.e(TAG, "getDetailStatisResult statisInfoList is null, so return null");
                return null;
            }

            Log.i(TAG, "getDetailStatisResult statisInfoList size is " + statisInfoList.size());

            List<String> resultList = new LinkedList<String>();
            // 主叫URI 呼叫次数 忙碌次数 拒绝次数 呼通率
            resultList.add(fixedWidth("主叫URI", 16)
                    + fixedWidth("呼叫次数")
                    + fixedWidth("通话次数")
                    + fixedWidth("忙碌次数")
                    + fixedWidth("拒绝次数")
                    + fixedWidth("呼通率"));


            if (statisInfoList.size() < 2)
            {
                resultList.add("====结果内容为空====");
            }
            else
            {
                int totalNum = statisInfoList.size();
                int currNum = 0;
                int progress = 0;

                for (StatisInfo tmp : statisInfoList)
                {
                    if (isCanceled)
                    {
                        return null;
                    }

                    resultList.add(fixedWidth(tmp.getCallerUri().replaceAll("[^0-9]", ""), 16)
                            + fixedWidth("" + tmp.getTotalNum())
                            + fixedWidth("" + tmp.getConnectedNum())
                            + fixedWidth("" + tmp.getBusyNum())
                            + fixedWidth("" + tmp.getRefusedNum())
                            + fixedWidth("" + tmp.getSuccessRate() + "%"));

                    currNum++;
                    if (90 + 10 * currNum / totalNum != progress)
                    {
                        progress = 90 + 10 * currNum / totalNum;
                        updateProgress(MSG_SHOW_STATIS_PB
                                , progress
                                , "输出结果格式化中" + "...".substring(currNum % 3));
                    }
                }
            }

            appendToStatisInfoBuffer(time + "输出结果格式化中...");
            appendToStatisInfoBuffer(getCurrTime() + "格式化完成");

            return resultList;
        }

        private String fixedWidth(String src)
        {
            return fixedWidth(src, 14);
        }

        private String fixedWidth(String src, int width)
        {
            int srcLen = src.getBytes().length;
            int blankLen = " ".getBytes().length;
            try
            {
                srcLen = src.getBytes("UTF-8").length;
                blankLen = " ".getBytes("UTF-8").length;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            int leftBlankNum = (width - srcLen) / 2;
            int rightBlankNum = width - srcLen - leftBlankNum;

            for (int i=0; i<leftBlankNum; i++)
            {
                src = " " + src;
            }
            for (int i=0; i<rightBlankNum; i++)
            {
                src = src + " ";
            }

            return src;
        }

        private String fixedWidth2(String src, int width)
        {
            int srcLen = src.getBytes().length;
            try
            {
                srcLen = src.getBytes("UTF-8").length;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            int len = (width - srcLen) / 2;
            for (int i = 0; i < len; i++)
            {
                src = " " + src + " ";
            }

            if (len < 0)
            {
                src = " " + src + " ";
            }

            srcLen = src.getBytes().length;
            try
            {
                srcLen = src.getBytes("UTF-8").length;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            int brankLen = width - srcLen;
            for (int i=0; i<brankLen; i++)
            {
                src = " " + src;
            }

            return src;
        }

        private boolean check()
        {
            if (null == recordInfoList || recordInfoList.size() <= 0)
            {
                appendToStatisInfoBuffer(getCurrTime() + "recordInfoList is empty");
                return false;
            }

            return true;
        }
    }

    private class DetailThread extends Thread
    {
        private String detailAccount;

        public DetailThread(String detailAccount)
        {
            this.detailAccount = detailAccount;
        }

        private boolean isCanceled;

        public boolean isCanceled()
        {
            return isCanceled;
        }

        public synchronized void cancel()
        {
            if (isCanceled)
            {
                return;
            }
            isCanceled = true;

            if (null != handler)
            {
                handler.sendEmptyMessageDelayed(MSG_HIDE_DETAIL_PB, 2 * 1000);
            }
        }
        @Override
        public void run()
        {
            detailInfoBuffer = new StringBuffer();

            appendToDetailInfoBuffer(getCurrTime() + "详情信息查询开始\r\n");

            // 20160811100345|A|sip:10039081@zte.com.cn|sip:10193455@zte.com.cn|28|0|CallEnd_Success
            appendToDetailInfoBuffer("时间|终端类型|主叫账号|被叫账号|通话时长|结果码|结果描述|是否接通");

            boolean isHasData = false;
            Set<String> set = map.keySet();
            Collection<List<RecordInfo>> values = map.values();
            int totalNum = set.size();
            int currNum = 0;
            int progress = 0;
            for (String key : set)
            {
                if (TextUtils.isEmpty(detailAccount) || (null != key && -1 != key.indexOf(detailAccount)))
                {
                    List<RecordInfo> list = new LinkedList<RecordInfo>();
                    List<RecordInfo> callerList = map.get(key);
                    if (null != callerList)
                    {
                        list.addAll(callerList);
                        list.add(null);
                    }

                    if (null != values && values.size() > 0)
                    {
                        for (List<RecordInfo> listTmp : values)
                        {
                            if (null != listTmp && listTmp.size() > 0)
                            {
                                for (RecordInfo infoTmp : listTmp)
                                {
                                    if (null != infoTmp && key.equals(infoTmp.getReceiverUri()))
                                    {
                                        Log.d(TAG, "add receiver key=" + key + ", infoTmp=" + infoTmp);
                                        list.add(infoTmp);
                                    }
                                }
                            }
                        }
                    }

                    if (null != list && list.size() > 0)
                    {
                        for (RecordInfo info : list)
                        {
                            if (null == info)
                            {
                                appendToDetailInfoBuffer("--------------------------------------------------------------------");
                                continue;
                            }
                            String record = info.getTime()
                                    + "|" + info.getType()
                                    + "|" + info.getCallerUri()
                                    + "|" + info.getReceiverUri()
                                    + "|" + info.getDuration();

                            if (null != info.getResultCode())
                            {
                                record += "|" + info.getResultCode();
                                if (null != info.getResultDesc())
                                {
                                    record += "|" + info.getResultDesc();
                                    if (null != info.getRinged())
                                    {
                                        record += "|" + info.getRinged();
                                    }
                                }
                            }
                            appendToDetailInfoBuffer(record);
                            isHasData = true;
                        }
                        appendToDetailInfoBuffer("\r\n");
                    }
                }

                currNum++;
                if (100 * currNum / totalNum != progress)
                {
                    Log.i(TAG, "progress=" + progress);
                    progress = 100 * currNum / totalNum;
                    updateProgress(MSG_SHOW_DETAIL_PB
                            , progress
                            , "详情信息查询中" + "...".substring(currNum % 3));
                }
            }

            if (!isHasData)
            {
                appendToDetailInfoBuffer("====没有查询到相关信息====");
            }

            appendToDetailInfoBuffer(getCurrTime() + "详情信息查询结束");

            // 正常结束 了
            cancel();
            // isOneKeyStart = false; // 放到消息handler中再改
        }
    }

    private class RecordInfo
    {
        // 20160811100345|A|sip:10039081@zte.com.cn|sip:10193455@zte.com.cn|28|0|CallEnd_Success
        private String time;
        private String type;
        private String callerUri;
        private String receiverUri;
        private Integer duration;
        private Integer resultCode;
        private String resultDesc;
        private Boolean Ringed;

        public RecordInfo(String record)
        {
            if (null == record)
            {
                return;
            }

            String[] fields = record.split("\\|");
            if (null == fields)
            {
                return;
            }

            int len = fields.length;
            for (int i = 0; i < len; i++)
            {
                setFieldValue(i, fields[i]);
            }
        }

        private void setFieldValue(int index, String value)
        {
            switch (index)
            {
                case 0:
                {
                    time = value;
                    break;
                }
                case 1:
                {
                    type = value;
                    break;
                }
                case 2:
                {
                    callerUri = value;
                    break;
                }
                case 3:
                {
                    receiverUri = value;
                    break;
                }
                case 4:
                {
                    try
                    {
                        duration = Integer.parseInt(value);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    break;
                }
                case 5:
                {
                    try
                    {
                        resultCode = Integer.parseInt(value);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    break;
                }
                case 6:
                {
                    resultDesc = value;
                    break;
                }
                case 7:
                {
                    if ("0".equals(value))
                    {
                        Ringed = false;
                    }
                    else if ("1".equals(value))
                    {
                        Ringed = true;
                    }
                }
                default:
                {
                    break;
                }
            }
        }

        @Override
        public String toString()
        {
            return "RecordInfo{" +
                    "time='" + time + '\'' +
                    ", type='" + type + '\'' +
                    ", callerUri='" + callerUri + '\'' +
                    ", receiverUri='" + receiverUri + '\'' +
                    ", duration=" + duration +
                    ", resultCode=" + resultCode +
                    ", resultDesc='" + resultDesc + '\'' +
                    ", Ringed='" + Ringed + '\'' +
                    '}';
        }

        public Boolean getRinged()
        {
            return Ringed;
        }

        public String getTime()
        {
            return time;
        }

        public String getType()
        {
            return type;
        }

        public String getCallerUri()
        {
            return callerUri;
        }

        public String getReceiverUri()
        {
            return receiverUri;
        }

        public Integer getDuration()
        {
            return duration;
        }

        public Integer getResultCode()
        {
            return resultCode;
        }

        public String getResultDesc()
        {
            return resultDesc;
        }
    }

    private class SelectInfo
    {
        private String startDate;
        private String endDate;
        private String caller;
        private String receiver;
        private int MinConnectedTime;
        private int MaxConnectedTime;
        private List<String> clientTypeList;

        public SelectInfo(String startDate
                , String endDate
                , String caller
                , String receiver
                , int minConnectedTime
                , int maxConnectedTime
                , List<String> clientTypeList)
        {
            this.startDate = startDate;
            this.endDate = endDate;
            this.caller = caller;
            this.receiver = receiver;
            MinConnectedTime = minConnectedTime;
            MaxConnectedTime = maxConnectedTime;
            this.clientTypeList = clientTypeList;
        }

        @Override
        public String toString()
        {
            return "SelectInfo{" +
                    "startDate='" + startDate + '\'' +
                    ", endDate='" + endDate + '\'' +
                    ", caller='" + caller + '\'' +
                    ", receiver='" + receiver + '\'' +
                    ", MinConnectedTime=" + MinConnectedTime +
                    ", MaxConnectedTime=" + MaxConnectedTime +
                    ", clientTypeList=" + clientTypeList +
                    '}';
        }

        public String getStartDate()
        {
            return startDate;
        }

        public String getEndDate()
        {
            return endDate;
        }

        public String getCaller()
        {
            return caller;
        }

        public String getReceiver()
        {
            return receiver;
        }

        public int getMinConnectedTime()
        {
            return MinConnectedTime;
        }

        public int getMaxConnectedTime()
        {
            return MaxConnectedTime;
        }

        public List<String> getClientTypeList()
        {
            return clientTypeList;
        }
    }

    private class StatisInfo
    {
        private String callerUri;
        private int totalNum;
        private int connectedNum;
        private int refusedNum;
        private int busyNum;
        private int callerSbcFailNum;
        private int receiverSbcFailNum;

        public void add(Integer duration, Integer resultCode)
        {
            totalNum++;

            if (null != duration && duration > 0)
            {
                connectedNum++;
                return;
            }

            if (null == resultCode)
            {
                // connectedNum++;
                return;
            }

            switch (resultCode)
            {
                /*
                case 0:
                {
                    connectedNum++;
                    break;
                }
                */
                case 2:
                case 3:
                case 4:
                {
                    callerSbcFailNum++;
                    break;
                }
                case 13:
                case 14:
                {
                    receiverSbcFailNum++;
                    break;
                }
                case 15:
                {
                    refusedNum++;
                    break;
                }
                case 16:
                case 17:
                {
                    busyNum++;
                    break;
                }
                default:
                {
                    break;
                }
            }
        }

        public void setCallerUri(java.lang.String callerUri)
        {
            this.callerUri = callerUri;
        }

        public String getCallerUri()
        {
            return callerUri;
        }

        public int getTotalNum()
        {
            return totalNum;
        }

        public int getConnectedNum()
        {
            return connectedNum;
        }

        public int getRefusedNum()
        {
            return refusedNum;
        }

        public int getBusyNum()
        {
            return busyNum;
        }

        public int getCallerSbcFailNum()
        {
            return callerSbcFailNum;
        }

        public int getReceiverSbcFailNum()
        {
            return receiverSbcFailNum;
        }

        public int getSuccessRate()
        {
            int rate = 100;

            if (0 != totalNum)
            {
                rate = 100 * (connectedNum + refusedNum + busyNum) / totalNum;
            }

            return rate;
        }

        @Override
        public String toString()
        {
            return "StatisInfo{" +
                    "callerUri=" + callerUri +
                    ", totalNum=" + totalNum +
                    ", connectedNum=" + connectedNum +
                    ", refusedNum=" + refusedNum +
                    ", busyNum=" + busyNum +
                    ", callerSbcFailNum=" + callerSbcFailNum +
                    ", receiverSbcFailNum=" + receiverSbcFailNum +
                    '}';
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (null != syncDataThread)
        {
            syncDataThread.cancel();
        }

        if (null != selectDataThread)
        {
            selectDataThread.cancel();
        }

        if (null != statisDataThread)
        {
            statisDataThread.cancel();
        }

        if (null != detailThread)
        {
            detailThread.cancel();
        }

        if (null != handler)
        {
            handler.removeCallbacksAndMessages(null);
        }
    }

}
