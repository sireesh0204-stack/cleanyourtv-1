package com.gathin.tvhome;
/** Pixel selection registered to the private Zenitsu artwork; never paints the character. */
final class LightningMask {
 static final float[] BODY={.546f,.108f,.606f,.092f,.662f,.112f,.705f,.185f,.850f,.167f,1f,.170f,1f,.430f,.909f,.451f,.920f,.555f,.955f,.630f,.942f,.696f,.962f,.732f,.970f,.789f,.919f,.823f,.895f,.740f,.838f,.773f,.770f,.790f,.728f,.818f,.657f,.778f,.588f,.727f,.554f,.743f,.508f,.780f,.468f,.816f,.402f,.817f,.405f,.757f,.445f,.698f,.465f,.630f,.439f,.591f,.430f,.542f,.445f,.473f,.483f,.406f,.541f,.376f,.563f,.340f,.546f,.289f,.536f,.212f};
 static int alpha(int color,float x,float y){
  int r=(color>>16)&255,g=(color>>8)&255,b=color&255;
  boolean white=r>=244&&g>=240&&b>=225;
  if((color>>>24)==0 || protectedArtwork(x,y) || r<165||g<95||(!white&&(r-b<12||g-b<6)))return 0;
  return Math.min(255,Math.max(0,Math.min((r-160)*4,(g-90)*3)));
 }
 static boolean protectedArtwork(float x,float y) { return (x>.93f&&y>.64f)||insideBody(x,y); }
 static float distanceFromBody(float x, float y, int w, int h) {
  float closest = Float.MAX_VALUE;
  for (int i = 0, j = BODY.length - 2; i < BODY.length; j = i, i += 2) {
   float ax = BODY[j] * w, ay = BODY[j + 1] * h;
   float dx = BODY[i] * w - ax, dy = BODY[i + 1] * h - ay;
   float t = Math.max(0, Math.min(1, ((x - ax) * dx + (y - ay) * dy) / (dx * dx + dy * dy)));
   float ex = x - ax - t * dx, ey = y - ay - t * dy;
   closest = Math.min(closest, ex * ex + ey * ey);
  }
  return (float) Math.sqrt(closest);
 }
 private static boolean insideBody(float x,float y){
  boolean inside=false;int count=BODY.length/2;
  for(int i=0,j=count-1;i<count;j=i++){
   float xi=BODY[i*2],yi=BODY[i*2+1],xj=BODY[j*2],yj=BODY[j*2+1];
   if((yi>y)!=(yj>y)&&x<(xj-xi)*(y-yi)/(yj-yi)+xi)inside=!inside;
  }
  return inside;
 }
}
