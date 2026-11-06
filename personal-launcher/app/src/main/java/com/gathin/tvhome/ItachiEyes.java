package com.gathin.tvhome;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/** Eye registration from the verified 1672x941 Itachi artwork, including the hair occlusion. */
final class ItachiEyes {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path iris = new Path(), near = new Path(), far = new Path();
    ItachiEyes() {
        near.moveTo(1033,328);near.quadTo(1075,278,1143,274);near.quadTo(1145,322,1050,331);near.close();
        far.moveTo(852,353);far.quadTo(878,333,915,367);far.quadTo(883,391,861,383);far.close();
    }
    void draw(Canvas c,int width,int height,float gazeX,float gazeY,float blink) {
        float scale=Math.max(width/1672f,height/941f);
        c.save();c.translate((width-1672*scale)/2f,(height-941*scale)/2f);c.scale(scale,scale);
        eye(c,875,366,13,gazeX,gazeY);eye(c,1069,306,21,gazeX,gazeY);
        if(blink>0){lid(c,near,274,331,blink);lid(c,far,333,391,blink);}c.restore();
    }
    private void eye(Canvas c,float x,float y,float radius,float gx,float gy) {
        c.save();iris.reset();iris.addCircle(x,y,radius,Path.Direction.CW);c.clipPath(iris);
        if(radius>20)c.clipOutRect(1041,y-30,1053,y+30);
        paint.setStyle(Paint.Style.FILL);paint.setColor(0xFFBF1238);c.drawCircle(x,y,radius,paint);
        c.translate(x+gx*radius*.18f,y+gy*radius*.12f);c.scale(radius/24f,radius/24f);c.rotate(gx*18);
        paint.setColor(0xFF280A17);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.2f);c.drawCircle(0,0,15,paint);
        paint.setStyle(Paint.Style.FILL);c.drawCircle(0,0,6,paint);
        for(int i=0;i<3;i++){c.drawCircle(0,-15,4.2f,paint);c.drawOval(-4.2f,-20,-.8f,-13,paint);c.rotate(120);}c.restore();
    }
    private void lid(Canvas c,Path path,float top,float bottom,float blink) {
        c.save();c.clipPath(path);paint.setColor(0xFF454F66);float mid=(top+bottom)/2;
        c.drawRect(830,top,1150,top+(mid-top)*blink,paint);
        c.drawRect(830,bottom-(bottom-mid)*blink,1150,bottom,paint);c.restore();
    }
}
