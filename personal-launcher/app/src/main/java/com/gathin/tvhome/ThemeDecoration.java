package com.gathin.tvhome;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/** Open strokes surround the logo; no plate or paint enters its cutouts. */
final class ThemeDecoration {
    private final CharacterStyle style;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    ThemeDecoration(CharacterStyle style) { this.style = style; }
    void draw(Canvas c, RectF r, float width, int alpha) {
        if (style == CharacterStyle.NONE) return;
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(width);
        paint.setStrokeCap(Paint.Cap.ROUND); paint.setColor(style.primary); paint.setAlpha(alpha);
        float x = r.centerX(), y = r.centerY(), w = r.width(), h = r.height();
        switch (style) {
            case GOJO:
                c.drawArc(r, 12, 128, false, paint);
                paint.setColor(style.secondary); paint.setAlpha(alpha);
                c.drawArc(r, 192, 128, false, paint); break;
            case SUKUNA:
                c.drawLine(r.left, r.top+h*.27f, r.left+w*.17f, r.top, paint);
                c.drawLine(r.right-w*.17f, r.bottom, r.right, r.bottom-h*.27f, paint);
                c.drawLine(r.left+w*.05f, r.top+h*.36f, r.left+w*.22f, r.top+h*.09f, paint); break;
            case ZENITSU:
                bolt(c, r.left, r.top+h*.12f, h*.72f, w*.08f);
                paint.setColor(style.secondary); paint.setAlpha(alpha);
                bolt(c, r.right, r.top+h*.12f, h*.72f, -w*.08f); break;
            case SAITAMA:
                c.drawArc(r, 10, 158, false, paint);
                c.drawArc(r, 190, 158, false, paint); break;
            case ITACHI:
                for (int i=0;i<3;i++) {
                    double a=i*Math.PI*2/3-.5;
                    c.drawCircle(x+(float)Math.cos(a)*w*.5f,y+(float)Math.sin(a)*h*.5f,width*1.6f,paint);
                } break;
            case BATMAN:
                path.reset(); path.moveTo(r.left,y+h*.12f);
                path.lineTo(r.left+w*.08f,y-h*.15f);path.lineTo(r.left+w*.18f,y-h*.06f);
                path.moveTo(r.right,y+h*.12f);path.lineTo(r.right-w*.08f,y-h*.15f);path.lineTo(r.right-w*.18f,y-h*.06f);
                c.drawPath(path,paint); break;
            case HOGWARTS:
                star(c,r.left,y,width*3);star(c,r.right,y,width*3); break;
            case DOOM:
                path.reset();path.moveTo(r.left+w*.15f,r.top);path.lineTo(r.right-w*.15f,r.top);
                path.lineTo(r.right,y);path.lineTo(r.right-w*.15f,r.bottom);path.lineTo(r.left+w*.15f,r.bottom);
                path.lineTo(r.left,y);path.close();c.drawPath(path,paint); break;
            default: break;
        }
        paint.setStyle(Paint.Style.FILL);
    }
    private void bolt(Canvas c,float x,float y,float height,float dx) {
        path.reset();path.moveTo(x,y);path.lineTo(x-dx,y+height*.36f);
        path.lineTo(x+dx*.6f,y+height*.42f);path.lineTo(x-dx*.6f,y+height);c.drawPath(path,paint);
    }
    private void star(Canvas c,float x,float y,float radius) {
        c.drawLine(x-radius,y,x+radius,y,paint);c.drawLine(x,y-radius,x,y+radius,paint);
    }
}
