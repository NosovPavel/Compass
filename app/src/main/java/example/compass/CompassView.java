package example.compass;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by nosovpavel on 09/10/14.
 */
public class CompassView extends View {

    private float bearing;

    private Paint markerPaint;
    private Paint textPaint;
    private Paint circlePaint;

    private String northString;
    private String eastString;
    private String southString;
    private String westString;

    private int textHeight;


    public CompassView(Context context) {
        super(context);
        initCompassView();
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCompassView();
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCompassView();
    }


    private void initCompassView() {
        setFocusable(true);

        Resources resources = getResources();

        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setColor(resources.getColor(R.color.marker_color));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(resources.getColor(R.color.text_color));

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(resources.getColor(R.color.background_color));
        circlePaint.setStrokeWidth(1);
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        northString = resources.getString(R.string.cardinal_north);
        eastString = resources.getString(R.string.cardinal_east);
        southString = resources.getString(R.string.cardinal_south);
        westString = resources.getString(R.string.cardinal_west);

        textHeight = (int)textPaint.measureText("yY");

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Компас должен занимать все доступное пространство
        int measuredWidth=measure(widthMeasureSpec);
        int measureHeight=measure(heightMeasureSpec);

        int d = Math.min(measuredWidth,measureHeight);

        setMeasuredDimension(d,d);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int measure(int measureSpec) {
        int result = 0;

        //Декодируем measureSpec
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode==MeasureSpec.UNSPECIFIED){
            //Если границы не указаны то вернем размер по умолчанию
            result = 200;
        } else {
            result = specSize;
        }
        return result;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int measuredWidth=getMeasuredWidth();
        int measureHeight=getMeasuredHeight();

        int px = measuredWidth/2;
        int py = measureHeight/2;

        int radius = Math.min(px,py);

        canvas.drawCircle(px,py,radius,circlePaint);
        canvas.save();
        canvas.rotate(-bearing,px,py);

        int textWidth = (int)textPaint.measureText("W");
        int cardinalX = px-textWidth/2;
        int cardinalY = py-radius+textHeight;

        //Рисуем отметки каждые 15 градусов и текст каждые 45

        for (int i = 0;i<24;i++){
            canvas.drawLine(px,py-radius,px,py-radius+10,markerPaint);

            canvas.save();
            canvas.translate(0,textHeight);

            //рисуем основные точки
            if(i%6==0){
                String dirString = "";
                switch (i){
                    case(0):
                        dirString = northString;
                        int arrowY = 2*textHeight;
                        canvas.drawLine(px,arrowY,px-5,3*textHeight,markerPaint);
                        canvas.drawLine(px,arrowY,px+5,3*textHeight,markerPaint);
                        break;

                    case(6):
                        dirString = eastString;
                        break;

                    case(12):
                        dirString = southString;
                        break;

                    case(18):
                        dirString = westString;
                        break;

                }
                canvas.drawText(dirString,cardinalX,cardinalY,textPaint);
            } else if(i%3==0) {
                //отображаем текст каждые 45 градусов
                String angle = String.valueOf(i*15);
                float angleTextWidth = textPaint.measureText(angle);

                int angleTextX = (int)(px-angleTextWidth/2);
                int angletextY = (int)px-radius+textHeight;

                canvas.drawText(angle,angleTextX,angletextY,textPaint);
            }
            canvas.restore();
            canvas.rotate(15,px,py);
        }
        canvas.restore();
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
         super.dispatchPopulateAccessibilityEvent(event);
        if(isShown()){
            String bearingstring= String.valueOf(bearing);

            if (bearingstring.length()>AccessibilityEvent.MAX_TEXT_LENGTH) {
                bearingstring = bearingstring.substring(0, AccessibilityEvent.MAX_TEXT_LENGTH);
            }
                event.getText().add(bearingstring);

                return true;
            } else {
                return false;
            }
    }
}
