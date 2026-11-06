package com.gathin.tvhome;

import android.graphics.*;

/** Bounded native effects: reusable paths/paints, shared wallpaper bitmap, no video or frame bitmaps. */
final class CharacterEffects {
    private final CharacterStyle style;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG), haze=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path(), clip=new Path();
    private final RectF rect=new RectF();
    private final Paint fog=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix fogMatrix=new Matrix();
    private final RadialGradient fogShader=new RadialGradient(0,0,1,
            new int[]{0x5AB9CDD9,0x18B9CDD9,0x00B9CDD9},new float[]{0,.5f,1},Shader.TileMode.CLAMP);
    private final float[] mesh=new float[(12+1)*(6+1)*2];
    private final ItachiEyes itachi;
    private int w,h;
    CharacterEffects(String id) { style=CharacterStyle.forTheme(id);itachi=style==CharacterStyle.ITACHI?new ItachiEyes():null;fog.setShader(fogShader); }
    void size(int width,int height) {
        w=width;h=height;
        float cx=style==CharacterStyle.DOOM?.49f:.81f;
        haze.setShader(new RadialGradient(w*cx,h*.42f,Math.max(1,w*.34f),
                new int[]{style.primary,style.primary&0xFFFFFF},null,Shader.TileMode.CLAMP));
    }
    void ambient(Canvas c,long now,Bitmap wallpaper,float gx,float gy) {
        if(w==0||h==0||style==CharacterStyle.NONE)return;
        float t=now/1000f;
        if((style==CharacterStyle.BATMAN||style==CharacterStyle.DOOM)&&wallpaper!=null)cape(c,wallpaper,t);
        paint.setStyle(Paint.Style.FILL);
        switch(style) {
            case GOJO:
                particles(c,t,30,.47f,1f,0xFF9DDFFF,false);
                paint.setColor(0x2874DCFF);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.4f);
                rect.set(w*.63f,h*.17f,w*.96f,h*.63f);c.drawArc(rect,t*3,210,false,paint);break;
            case SUKUNA:
                blood(c,t);break;
            case ZENITSU:
                // Long continuous envelopes replace strobing flashes; gold and ice-blue stay distinct.
                for(int i=0;i<3;i++) {
                    float a=.52f+i*.18f;
                    lightning(c,w*a,h*.22f,w*(a-.16f),h*.75f,t+i,i%2==0?style.primary:style.secondary,85+(int)(40*Math.sin(t*.85f+i)));
                }
                particles(c,t,15,.48f,1f,style.primary,false);break;
            case SAITAMA:
                haze.setAlpha(35+(int)(8*Math.sin(t*.7f)));c.drawRect(0,0,w,h,haze);
                particles(c,t,20,.56f,1f,style.primary,false);
                wind(c,t,style.secondary,45);break;
            case ITACHI:
                rain(c,t);itachi.draw(c,w,h,gx,gy,1-DomainView.blink(now));break;
            case BATMAN:
                mist(c,t,18);bats(c,t);break;
            case HOGWARTS:
                mist(c,t,13);particles(c,t,24,.12f,.97f,style.primary,false);
                candles(c,t);break;
            case DOOM:
                haze.setAlpha(23+(int)(7*Math.sin(t*.65f)));c.drawRect(0,0,w,h,haze);
                particles(c,t,18,.28f,.68f,style.primary,false);
                doomEyes(c,t);break;
            default:break;
        }
        paint.setStyle(Paint.Style.FILL);paint.setAlpha(255);
    }
    private void blood(Canvas c,float t) {
        // Four hand anchors, with droplets thinning into dark ash as they rise.
        for(int i=0;i<24;i++) {
            int hand=i%4;float ax=hand==0?.69f:hand==1?.93f:hand==2?.74f:.86f;
            float ay=hand<2?.61f:.37f;
            float p=(t*.11f+i*.618034f)%1;
            float x=w*ax+(float)Math.sin(i*2.4+t*.6)*w*.007f;
            float y=h*ay-p*h*(.12f+(i%3)*.025f);
            paint.setColor(p<.68f?0xFFAF1735:0xFF6E4B55);paint.setAlpha((int)(125*Math.sin(p*Math.PI)));
            float r=1.2f+i%3*.5f;c.drawOval(x-r,y-r*1.5f,x+r,y+r*1.5f,paint);
        }
    }
    private void particles(Canvas c,float t,int count,float left,float right,int color,boolean fall) {
        for(int i=0;i<count;i++) {
            float p=(t*(.018f+(i%4)*.003f)+i*.618034f)%1;
            float x=w*(left+(right-left)*((i*.381966f)%1))+(float)Math.sin(t*.3+i)*5;
            float y=h*(fall?p:1-p);
            paint.setStyle(Paint.Style.FILL);paint.setColor(color);paint.setAlpha((int)(85*Math.sin(p*Math.PI)));
            c.drawCircle(x,y,1+i%3*.45f,paint);
        }
    }
    private void lightning(Canvas c,float x,float y,float ex,float ey,float t,int color,int alpha) {
        path.reset();path.moveTo(x,y);
        for(int i=1;i<=10;i++) {
            float p=i/10f,offset=(float)Math.sin(i*4.7+t*.7)*w*.012f*(float)Math.sin(p*Math.PI);
            path.lineTo(x+(ex-x)*p+offset,y+(ey-y)*p);
        }
        paint.setStyle(Paint.Style.STROKE);paint.setColor(color);paint.setAlpha(alpha/3);paint.setStrokeWidth(7);c.drawPath(path,paint);
        paint.setAlpha(alpha);paint.setStrokeWidth(1.4f);c.drawPath(path,paint);paint.setStyle(Paint.Style.FILL);
    }
    private void wind(Canvas c,float t,int color,int alpha) {
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.3f);paint.setColor(color);paint.setAlpha(alpha);
        for(int i=0;i<4;i++) {
            float y=h*(.26f+i*.15f)+(float)Math.sin(t*.5+i)*7;
            path.reset();path.moveTo(w*.56f,y);path.cubicTo(w*.72f,y-25,w*.83f,y+22,w*1.02f,y-15);c.drawPath(path,paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }
    private void mist(Canvas c,float t,int alpha) {
        fog.setAlpha(alpha*3);
        for(int i=0;i<3;i++) {
            float x=(float)Math.sin(t*.09+i)*w*.13f;
            fogMatrix.setScale(w*.65f,h*.10f);
            fogMatrix.postTranslate(w*.48f+x,h*(.67f+i*.11f));
            fogShader.setLocalMatrix(fogMatrix);c.drawRect(0,0,w,h,fog);
        }
    }
    private void rain(Canvas c,float t) {
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1);paint.setColor(0x557CA0C8);
        for(int i=0;i<36;i++) {
            float x=w*((i*.381966f)%1),y=h*((t*.20f+i*.618034f)%1);
            c.drawLine(x,y,x-3,y+12,paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }
    private void bats(Canvas c,float t) {
        paint.setColor(0xB5080A0F);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2);
        for(int i=0;i<4;i++) {
            float p=(t*.025f+i*.23f)%1,x=w*(.40f+p*.57f),y=h*(.28f+i*.025f);
            float flap=3+(float)Math.sin(t*4+i)*3;
            path.reset();path.moveTo(x-8,y-flap);path.quadTo(x-4,y-3,x,y+2);path.quadTo(x+4,y-3,x+8,y-flap);c.drawPath(path,paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }
    private void candles(Canvas c,float t) {
        // Floating warm lights also suit the greenhouse and library variants without fixed window coordinates.
        for(int i=0;i<8;i++) {
            float x=w*(.23f+i*.09f),y=h*(.29f+(i%3)*.12f)+(float)Math.sin(t*.6+i)*h*.012f;
            paint.setColor(0x68F6CA73);c.drawOval(x-2,y-5,x+2,y+3,paint);
            paint.setColor(0x35FFECC4);c.drawRect(x-1,y+3,x+1,y+11,paint);
        }
    }
    private void doomEyes(Canvas c,float t) {
        paint.setColor(style.primary);paint.setAlpha(95+(int)(25*Math.sin(t*.9f)));
        // Both local Doom variants share the same 1920x1080 figure registration.
        c.drawOval(w*.474f,h*.118f,w*.482f,h*.122f,paint);
        c.drawOval(w*.489f,h*.118f,w*.497f,h*.122f,paint);
    }
    private void cape(Canvas c,Bitmap bitmap,float t) {
        clip.reset();
        if(style==CharacterStyle.BATMAN) {
            clip.moveTo(w*.26f,h*.67f);clip.lineTo(w*.61f,h*.38f);clip.lineTo(w,h*.36f);clip.lineTo(w,h);clip.lineTo(w*.30f,h);
        }else{
            clip.moveTo(w*.40f,h*.29f);clip.lineTo(w*.35f,h*.63f);clip.lineTo(w*.23f,h*.78f);clip.lineTo(w*.29f,h*.83f);clip.lineTo(w*.44f,h*.64f);
        }
        clip.close();int n=0;
        for(int row=0;row<=6;row++)for(int col=0;col<=12;col++) {
            float x=col/12f,y=row/6f;
            float shift=(float)(Math.sin(x*Math.PI)*Math.sin(y*Math.PI)*Math.sin(t*.7+y*5))*2.0f;
            mesh[n++]=x*w+shift;mesh[n++]=y*h+shift*.4f;
        }
        c.save();c.clipPath(clip);paint.setColor(Color.WHITE);paint.setAlpha(255);c.drawBitmapMesh(bitmap,12,6,mesh,0,null,0,paint);c.restore();
    }
    void reveal(Canvas c,float progress) {
        if(progress>=1||style==CharacterStyle.NONE)return;
        float ease=1-(1-progress)*(1-progress);
        paint.setStyle(Paint.Style.STROKE);paint.setColor(style.primary);paint.setAlpha((int)((1-progress)*150));paint.setStrokeWidth(2);
        if(style==CharacterStyle.ZENITSU) {
            lightning(c,w*.24f,h*.75f,w*.98f,h*.19f,progress*5,style.primary,(int)((1-progress)*160));
            lightning(c,w*.27f,h*.77f,w*.99f,h*.22f,progress*5,style.secondary,(int)((1-progress)*140));
        }else if(style==CharacterStyle.SUKUNA) {
            for(int i=0;i<2;i++){
                float p=CharacterStyle.phase((long)(progress*1200),i*230,450);
                paint.setAlpha((int)((1-p)*155));c.drawLine(w*(.54f+i*.12f),h*.18f,w*(.54f+i*.12f)+w*.24f*p,h*.18f+h*.46f*p,paint);
            }
        }else if(style==CharacterStyle.ITACHI) {
            c.save();c.translate(w*.5f,h*.45f);c.rotate(ease*240);float r=h*.085f;
            c.drawCircle(0,0,r,paint);for(int i=0;i<3;i++){c.drawCircle(0,-r*.6f,r*.15f,paint);c.rotate(120);}c.restore();
        }else {
            float x=style==CharacterStyle.SAITAMA?w*.59f:style==CharacterStyle.DOOM?w*.49f:w*.77f;
            c.drawCircle(x,h*.40f,ease*w*.67f,paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }
    void focus(Canvas c,float x,float y,float progress) {
        if(progress>=1||style==CharacterStyle.NONE)return;
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.5f);paint.setColor(style.primary);paint.setAlpha((int)((1-progress)*120));
        float rx=w*.035f*(1+progress*.4f),ry=h*.034f*(1+progress*.4f);
        if(style==CharacterStyle.ZENITSU) lightning(c,w*.75f,h*.48f,x,y,progress*4,style.secondary,(int)((1-progress)*100));
        else if(style==CharacterStyle.SUKUNA){c.drawLine(x-rx,y+ry,x+rx,y-ry,paint);}
        else {rect.set(x-rx,y-ry,x+rx,y+ry);c.drawOval(rect,paint);}
        paint.setStyle(Paint.Style.FILL);
    }
}
