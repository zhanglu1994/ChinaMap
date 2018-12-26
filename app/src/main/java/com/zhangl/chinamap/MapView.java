package com.zhangl.chinamap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;

import android.util.AttributeSet;
import android.view.View;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by zhangl on 2018/12/26.
 */

public class MapView extends View {



    private Context context;
    private RectF totalRect;
    private List<ProviceItem> itemList;
    private int[] colorArray = new int[]{0xFF239BD7, 0xFF30A9E5, 0xFF80CBF1, 0xFFFFFFFF};
    private Paint paint;
    private float scale=1.0f;
    private ProviceItem select;

    public MapView(Context context) {
        this(context,null);

    }

    public MapView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);

        this.context = context;
        init(context);


    }



    public MapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (itemList != null) {
            canvas.save();
            canvas.scale(scale,scale);
            for (ProviceItem proviceItem : itemList) {
                if (proviceItem != select) {
                    proviceItem.drawItem(canvas, paint, false);
                }
            }
            if (select != null) {
                select.drawItem(canvas, paint, true);
            }
        }

    }


    private void init(Context context) {
        paint = new Paint();
        paint.setAntiAlias(true);
        loadThread.start();

    }





    private Thread loadThread=new Thread() {
        @Override
        public void run() {
            InputStream inputStream = context.getResources().openRawResource(R.raw.china);
            List<ProviceItem> list = new ArrayList<>();
            try {

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  //取得DocumentBuilderFactory实例
                DocumentBuilder builder = null; //从factory获取DocumentBuilder实例
                builder = factory.newDocumentBuilder();
                Document doc = builder.parse(inputStream);   //解析输入流 得到Document实例
                Element rootElement = doc.getDocumentElement();
                NodeList items = rootElement.getElementsByTagName("path");
//                中国地图的  矩形
                float left = -1;
                float right = -1;
                float top = -1;
                float bottom = -1;
                for (int i = 0; i < items.getLength(); i++) {
                    Element element = (Element) items.item(i);
                    String pathData = element.getAttribute("android:pathData");
                    Path path = PathParser.createPathFromPathData(pathData);
                    ProviceItem proviceItem = new ProviceItem(path);
                    list.add(proviceItem);
//                    获取宽高
                    RectF rect = new RectF();
                    path.computeBounds(rect, true);
                    left = left == -1 ? rect.left : Math.min(left, rect.left);
                    right = right == -1 ? rect.right : Math.max(right, rect.right);
                    top = top == -1 ? rect.top : Math.min(top, rect.top);
                    bottom = bottom == -1 ? rect.bottom : Math.max(bottom, rect.bottom);
                    totalRect = new RectF(left, top, right, bottom);
                }
                itemList = list;
                handler.sendEmptyMessage(1);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        ;

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (itemList == null) {
                    return;
                }
                int totalNumber = itemList.size();
                for (int i = 0; i < totalNumber; i++) {
                    int color = Color.WHITE;
                    int flag = i % 4;
                    switch (flag) {
                        case 1:
                            color = colorArray[0];
                            break;
                        case 2:
                            color = colorArray[1];
                            break;
                        case 3:
                            color = colorArray[2];
                            break;
                        default:
                            color = Color.CYAN;
                            break;
                    }
                    itemList.get(i).setDrawColor(color);
                }
                postInvalidate();
            }
        };
    };






}



