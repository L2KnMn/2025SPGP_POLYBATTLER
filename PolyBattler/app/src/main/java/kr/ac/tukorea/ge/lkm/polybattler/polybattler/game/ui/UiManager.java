package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.IGameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class UiManager implements IGameManager {
    public static final Map<Scene, UiManager> instances = new HashMap<>();
    private final Scene master;
    private GameState currentState = GameState.PREPARE; // 초기 상태 설정
    private final List<IGameObject> uiObjects; // 관리할 UI 요소 리스트
    private final List<Button> buttons; // 터치 이벤트를 받을 버튼 리스트
    private final Paint textPaint; // 텍스트용 Paint
    private final Paint backgroundPaint; // 배경용 Paint
    private final String TAG = "UiManager";

    // ToastMessenger 구현
    protected class ToastMessenger implements IGameObject {
        private String message = null;
        private float displayTime = 0; // 남은 표시 시간 (초)
        private final float DURATION = 2.0f; // 토스트 메시지 표시 시간
        private final Transform transform;
        private float textOffsetY;

        ToastMessenger(){
            transform = new Transform(this);
            // 초기에는 Scene에 추가하지 않음. show() 할 때 추가
        }

        // 메시지를 표시하는 함수
        void show(String string){
            message = string;
            displayTime = DURATION;

            // 위치 및 크기 계산 (예시: 화면 하단 중앙)
            float width = textPaint.measureText(message) + 40; // 좌우 여백
            float height = 80; // 고정 높이 또는 텍스트 높이 기반
            float screenWidth = Metrics.width;
            float screenHeight = Metrics.height;

            float left = (screenWidth - width) / 2;
            float top = screenHeight * 0.8f; // 화면 높이의 80% 지점

            // 텍스트 중앙 정렬을 위한 위치 계산
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float textX = left + width/2;
            textOffsetY = - (fm.ascent + fm.descent) / 2;
            float textY = top + height/2;
            transform.set(textX, textY);
            transform.setSize(width, height);

            // Scene에 추가하여 그리도록 함
            if (!master.contains(this)) {
                master.add(this);
            }
            Log.d(TAG, message);
        }

        @Override
        public void update() {
            if (message != null && displayTime > 0) {
                displayTime -= GameView.frameTime; // 프레임 시간만큼 감소
                // 조금씩 위로 올라가는 애니메이션
                final float SPEED = 10.0f; // 애니메이션 속도
                transform.move(0, -SPEED * GameView.frameTime);
                if (displayTime <= 0) {
                    this.reset();
                    master.remove(this); // Scene에서 제거
                    Log.d(TAG, "Toast Hide");
                }
            }
        }

        public void reset(){
            message = null;
            displayTime = 0;
        }

        @Override
        public void draw(Canvas canvas) {
            if (message != null && displayTime > 0) {
                // 반투명 배경 그리기
                backgroundPaint.setColor(Color.argb(180, 0, 0, 0)); // 반투명 검정
                canvas.drawRoundRect(transform.getRect(), 20, 20, backgroundPaint); // 둥근 모서리

                // 텍스트 그리기
                textPaint.setColor(Color.WHITE);
                textPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(message, transform.getPosition().x, transform.getPosition().y + textOffsetY, textPaint);
            }
        }
    }
    ToastMessenger toast;

    // Signage 구현
    protected class Signage implements IGameObject {
        String text;
        Transform transform; // 위치와 크기 관리
        boolean[] visible; // 게임 상태별 가시성
        private final Paint signTextPaint; // Signage 텍스트용 Paint
        private final Paint signBackgroundPaint; // Signage 배경용 Paint
        private final RectF textBounds = new RectF(); // 텍스트 영역 계산용

        Signage(String string, float x, float y, float width, float height) {
            text = string;
            transform = new Transform(this, x, y); // Transform 초기화
            transform.setSize(width, height); // 크기 설정
            visible = new boolean[GameState.values().length];
            // 기본적으로 모든 상태에서 보이지 않도록 설정
            Arrays.fill(visible, false);

            signTextPaint = new Paint(textPaint);
            signBackgroundPaint = new Paint(backgroundPaint);
            signBackgroundPaint.setColor(Color.DKGRAY); // 기본 배경색 설정
            signTextPaint.setColor(Color.WHITE);      // 기본 텍스트색 설정
            signTextPaint.setTextSize(40);            // 기본 텍스트 크기 설정

            master.add(this); // Scene에 추가
            uiObjects.add(this); // 관리 목록에 추가
        }

        // 특정 게임 상태에서 보이도록 설정
        public void setVisibility(GameState state, boolean isVisible) {
            if (state.ordinal() < visible.length) {
                visible[state.ordinal()] = isVisible;
            }
        }

        // 텍스트 설정
        public void setText(String newText) {
            this.text = newText;
        }

        // 위치 설정
        public void setPosition(float x, float y) {
            transform.set(x, y);
        }

        // 크기 설정
        public void setSize(float width, float height) {
            transform.setSize(width, height);
        }

        // 색상 설정
        public void setColors(int backgroundColor, int textColor) {
            signBackgroundPaint.setColor(backgroundColor);
            signTextPaint.setColor(textColor);
        }

        @Override
        public void update() {
            // Signage는 보통 상태 변경 외에 특별한 업데이트 로직이 필요 없을 수 있음
            // 필요하다면 애니메이션 등을 추가
        }

        @Override
        public void draw(Canvas canvas) {
            // 현재 게임 상태에서 보여야 하는 경우에만 그림
            if (currentState != null && currentState.ordinal() < visible.length && visible[currentState.ordinal()]) {
                RectF drawRect = transform.getRect(); // Transform에서 현재 사각형 영역 가져오기

                // 배경 그리기
                canvas.drawRect(drawRect, signBackgroundPaint);

                // 텍스트 중앙 정렬 계산 및 그리기
                signTextPaint.setTextAlign(Paint.Align.CENTER);
                Paint.FontMetrics fm = signTextPaint.getFontMetrics();
                float textY = drawRect.centerY() - (fm.ascent + fm.descent) / 2;
                canvas.drawText(text, drawRect.centerX(), textY, signTextPaint);
            }
        }
    }
    // Button 구현
    protected class Button implements IGameObject {
        private final Transform transform;
        private final String text; // 버튼 텍스트 (이미지 버튼도 가능하도록 확장 가능)
        private final Runnable action; // 버튼 클릭 시 실행될 동작
        private final Paint buttonPaint;
        private final Paint buttonTextPaint;
        private boolean isPressed = false; // 버튼이 눌린 상태인지
        private final RectF touchArea = new RectF(); // 터치 영역

        // 버튼 생성자 (텍스트 기반)
        Button(String text, float x, float y, float width, float height, Runnable action){
            this.text = text;
            this.transform = new Transform(this, x, y);
            this.transform.setSize(width, height);
            this.action = action;

            this.buttonPaint = new Paint();
            this.buttonTextPaint = new Paint(textPaint); // 기본 텍스트 페인트 복사 사용
            this.buttonTextPaint.setColor(Color.BLACK);
            this.buttonTextPaint.setTextAlign(Paint.Align.CENTER);

            master.add(this); // Scene에 추가
            uiObjects.add(this); // 관리 목록에 추가
            buttons.add(this); // 버튼 리스트에 추가
        }

        // 이미지 기반 버튼 생성자 (추가 구현 필요)
        // Button(int bitmapResId, float x, float y, float width, float height, Runnable action) { ... }

        @Override
        public void update() {
            // 버튼 상태에 따른 시각적 변화 등 (예: 눌렸을 때 색 변경)
        }

        @Override
        public void draw(Canvas canvas) {
            RectF drawRect = transform.getRect();

            // 상태에 따라 버튼 색상 변경
            if (isPressed) {
                buttonPaint.setColor(Color.LTGRAY); // 눌렸을 때 색
            } else {
                buttonPaint.setColor(Color.GRAY); // 기본 색
            }
            canvas.drawRect(drawRect, buttonPaint);

            // 텍스트 그리기
            Paint.FontMetrics fm = buttonTextPaint.getFontMetrics();
            float textY = drawRect.centerY() - (fm.ascent + fm.descent) / 2;
            canvas.drawText(text, drawRect.centerX(), textY, buttonTextPaint);
        }

        public RectF getTouchArea() {
            touchArea.set(transform.getRect());
            // touchArea.inset(-10, -10);
            return touchArea;
        }

        // 터치 이벤트 처리 (UiManager의 onTouch에서 호출될 것)
        public boolean handleTouchEvent(MotionEvent event, float x, float y) {
            RectF area = getTouchArea();
            boolean contains = area.contains(x, y);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (contains) {
                        isPressed = true;
                        Log.d(TAG, "Button Pressed: " + text);
                        return true; // 이 버튼이 이벤트를 소비함
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    // 누른 상태에서 버튼 밖으로 나가면 눌림 상태 해제
                    if (isPressed && !contains) {
                        isPressed = false;
                        Log.d(TAG, "Button Press Canceled: " + text);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (isPressed && contains) {
                        isPressed = false;
                        // 액션 실행!
                        if (action != null) {
                            Log.d(TAG, "Button Clicked: " + text);
                            action.run();
                        }
                        return true; // 이 버튼이 이벤트를 소비함
                    }
                    // UP 이벤트가 버튼 밖에서 발생했으면 눌림 상태 해제만 하고 실행 않기
                    if (isPressed) {
                        isPressed = false;
                    }
                    break;
            }
            return false; // 이 버튼이 이벤트를 소비하지 않음 (DOWN에서 true 반환했을 경우 제외)
        }
    }

    // UiManager 생성자
    private UiManager(Scene master) {
        this.master = master;
        this.uiObjects = new ArrayList<>();
        this.buttons = new ArrayList<>();

        // 기본 Paint 설정
        textPaint = new Paint();
        textPaint.setTextSize(40); // 기본 텍스트 크기
        textPaint.setColor(Color.WHITE); // 기본 텍스트 색상
        textPaint.setAntiAlias(true);

        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);

        toast = new ToastMessenger(); // Toast 객체 생성
        // Scene에는 show() 할 때 추가/제거
    }

    public static UiManager getInstance(Scene master) {
        return instances.computeIfAbsent(master, UiManager::new);
    }

    // IGameManager 인터페이스 구현: 터치 이벤트 처리
    @Override
    public boolean onTouch(MotionEvent event) {
        float[] xy = Metrics.fromScreen(event.getX(), event.getY());
        float x = xy[0];
        float y = xy[1];

        // 버튼들에 터치 이벤트 전달 (역순으로 전달하여 위에 있는 버튼이 먼저 받도록 함)
        for (int i = buttons.size() - 1; i >= 0; i--) {
            Button button = buttons.get(i);
            // 현재 게임 상태에서 버튼이 활성화되어 있는지 확인 (필요하다면 Button 클래스에 상태별 활성화 기능 추가)
            // 예: if (button.isVisibleInState(currentState)) { ... }
            if (button.handleTouchEvent(event, x, y)) {
                // 특정 버튼이 터치 이벤트를 처리했다면 더 이상 다른 UI 요소나 게임 로직으로 전달하지 않음
                return false; // 이벤트 소비됨 (false 반환 시 Scene의 다른 객체는 받지 않음)
            }
        }

        // 어떤 버튼도 이벤트를 처리하지 않았으면 다른 Manager가 처리하도록 함
        return true; // 이벤트 계속 전파 (true 반환 시 Scene의 다른 객체도 받음)
    }

    // IGameManager 인터페이스 구현: 게임 상태 설정
    @Override
    public void setGameState(GameState state) {
        Log.d(TAG, "GameState changed to: " + state);
        this.currentState = state;
        // 관리하는 UI 요소들에게 상태 변경 알림 (필요시)
        // 예: Signage는 draw에서 상태를 확인하므로 별도 알림 불필요
        // 버튼 활성화/비활성화 로직이 필요하다면 여기서 처리
    }

    // IGameManager 인터페이스 구현: 현재 게임 상태 반환
    @Override
    public GameState getGameState() {
        return currentState;
    }

    // --- 공개 메소드 (UI 요소 추가/관리용) ---

    // Toast 메시지 표시
    public void showToast(String message) {
        toast.show(message);
    }

    // Signage 생성 및 추가
    public Signage addSignage(String text, float x, float y, float width, float height) {
        Signage sign = new Signage(text, x, y, width, height);
        // uiObjects 리스트에는 생성자에서 이미 추가됨
        return sign;
    }

    // Button 생성 및 추가
    public Button addButton(String text, float x, float y, float width, float height, Runnable action) {
        Button button = new Button(text, x, y, width, height, action);
        // uiObjects 및 buttons 리스트에는 생성자에서 이미 추가됨
        return button;
    }

    // 특정 UI 요소 제거 (필요시)
    public void removeUIObject(IGameObject uiObject) {
        master.remove(uiObject);
        uiObjects.remove(uiObject);
        if (uiObject instanceof Button) {
            buttons.remove((Button) uiObject);
        }
    }

    // 모든 UI 요소 제거 (Scene 전환 시 등)
    public void clearAllUI() {
        // Scene에서 제거
        for (IGameObject obj : uiObjects) {
            master.remove(obj);
        }
        // 관리 리스트 비우기
        uiObjects.clear();
        buttons.clear();
        // Toast는 Scene에서 제거되지만 객체는 유지
        master.remove(toast);
        toast.message = null; // 메시지 내용도 초기화
    }
}