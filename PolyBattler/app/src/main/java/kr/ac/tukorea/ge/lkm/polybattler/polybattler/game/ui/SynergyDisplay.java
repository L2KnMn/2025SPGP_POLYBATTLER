package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree.BattleUnit;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.SynergyFactory;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

// Synergy 정보를 표시하는 내부 클래스
public class SynergyDisplay implements IGameObject, IEventHandle {
    private List<SynergyFactory.ActiveSynergy> activeSynergies = new ArrayList<>();
    // TODO: 다음 시너지 달성 정보 및 모든 시너지 타입 정보도 필요

    private final Transform transform; // 패널 자체의 위치와 크기
    private final Paint panelBackgroundPaint; // 패널 배경 Paint
    private final Paint sectionTitlePaint; // 섹션 제목 Paint
    private final Paint synergyItemPaint; // 시너지 항목 텍스트 Paint
    private final Paint progressBackgroundPaint; // 진행 바 배경 Paint
    private final Paint progressFillPaint; // 진행 바 채우기 Paint
    private final Paint activeTierPaint; // 활성화된 티어 표시 Paint

    private boolean isVisible = false; // 패널 가시성

    // 상세 정보 팝업 관련 변수
    private SynergyFactory.ActiveSynergy selectedSynergy = null; // 현재 선택된 시너지 (상세 보기용)
    private RectF detailPopupRect = new RectF();
    private Paint detailPopupBackgroundPaint;
    private Paint detailPopupTextPaint;
    private boolean showDetailPopup = false;
    private UiManager.Button openerButton;

    public SynergyDisplay(float x, float y, float width, float height) {
        transform = new Transform(this, x, y);
        transform.setSize(width, height);

        panelBackgroundPaint = new Paint();
        panelBackgroundPaint.setColor(Color.argb(180, 50, 50, 50)); // 반투명 어두운 회색 배경

        sectionTitlePaint = new Paint();
        sectionTitlePaint.setTextSize(Metrics.GRID_UNIT * 0.5f); // 섹션 제목 크기
        sectionTitlePaint.setColor(Color.YELLOW); // 섹션 제목 색상

        synergyItemPaint = new Paint();
        synergyItemPaint.setTextSize(Metrics.GRID_UNIT * (0.4f)); // 시너지 항목 텍스트 크기
        synergyItemPaint.setColor(Color.WHITE); // 시너지 항목 텍스트 색상

        progressBackgroundPaint = new Paint();
        progressBackgroundPaint.setColor(Color.GRAY); // 진행 바 배경색

        progressFillPaint = new Paint();
        progressFillPaint.setColor(Color.GREEN); // 진행 바 채우기 색상

        activeTierPaint = new Paint();
        activeTierPaint.setTextSize(Metrics.GRID_UNIT * (0.4f));
        activeTierPaint.setColor(Color.CYAN); // 활성화된 티어 표시 색상 (예: 별 색상)

        // 상세 정보 팝업 Paint 설정
        detailPopupBackgroundPaint = new Paint();
        detailPopupBackgroundPaint.setColor(Color.argb(220, 30, 30, 30)); // 어두운 배경
        detailPopupTextPaint = new Paint();
        detailPopupTextPaint.setTextSize(Metrics.GRID_UNIT * (0.35f));
        detailPopupTextPaint.setColor(Color.WHITE);
    }

    // 시너지 데이터 업데이트
    public void updateSynergies(List<SynergyFactory.ActiveSynergy> synergies) {
        this.activeSynergies = synergies;
        // TODO: 유닛 관리 시스템으로부터 현재 유닛 수를 받아와서 다음 시너지 달성 정보 업데이트 필요
    }

    // 패널 가시성 설정
    public void setVisibility(boolean visible) {
        this.isVisible = visible;
    }

    // 터치 이벤트 처리
    @Override
    public boolean handleTouchEvent(MotionEvent event, float x, float y) {
        if (!isVisible) return false; // 패널이 보이지 않으면 터치 처리 안 함

        RectF panelRect = transform.getRect();
        if (!panelRect.contains(x, y)) { // 패널 영역 밖 터치 하면 패널 닫음
            // 상태 초기화
            isVisible = false;
            showDetailPopup = false;
            selectedSynergy = null;
            // 버튼 다시 활성화
            setOpenerButtonVisibility(true);
            return true;
        }

        return false; // 패널 영역 내 터치지만 시너지 항목에 해당하지 않음
    }

    @Override
    public void update() {
        // TODO: 시너지 데이터 변경 감지 및 UI 업데이트 (필요시)
    }
    @Override
    public void draw(Canvas canvas) {
        if (!isVisible) return;

        RectF panelRect = transform.getRect();

        // 패널 배경 그리기
        canvas.drawRect(panelRect, panelBackgroundPaint);

        float currentY = panelRect.top + Metrics.GRID_UNIT * (0.5f); // 시작 Y 위치
        float itemStartX = panelRect.left + Metrics.GRID_UNIT * (0.2f); // 항목 시작 X 위치
        float itemWidth = panelRect.width() - Metrics.GRID_UNIT * (0.4f); // 항목 너비
        float itemHeight = Metrics.GRID_UNIT * (0.5f); // 항목 높이 예시

        // ShapeType 섹션 제목
        sectionTitlePaint.setTextAlign(Paint.Align.LEFT); // 섹션 제목 좌측 정렬
        canvas.drawText("모양 시너지", itemStartX, currentY + sectionTitlePaint.getTextSize() / 2, sectionTitlePaint);
        currentY += Metrics.GRID_UNIT * (0.6f); // 다음 요소 Y 위치

        synergyItemPaint.setTextAlign(Paint.Align.LEFT); // 시너지 항목 텍스트 좌측 정렬
        activeTierPaint.setTextAlign(Paint.Align.LEFT); // 별 좌측 정렬 (synergyItemPaint와 같은 정렬 사용)


        // ShapeType 시너지 항목 그리기
        for (SynergyFactory.ActiveSynergy synergy : activeSynergies) {
            if (synergy.getShapeType() != null) {
                String text = getSynergyText(synergy);
                float textX = itemStartX + Metrics.GRID_UNIT * (0.8f); // 아이콘 공간 확보
                float maxTextWidth = panelRect.right - textX - Metrics.GRID_UNIT * (0.2f) - activeTierPaint.measureText("★".repeat(synergy.getTier()) + " (99/99)"); // 텍스트 최대 너비 (별과 조건 표시 공간 제외)

                // 텍스트가 패널 너비를 벗어나는지 확인하고 잘라내기
                String displayedText = text;
                if (synergyItemPaint.measureText(text) > maxTextWidth) {
                    // 텍스트를 maxTextWidth에 맞게 자르고 ... 추가
                    int numChars = synergyItemPaint.breakText(text, true, maxTextWidth, null);
                    displayedText = text.substring(0, numChars) + "...";
                }

                // 아이콘 그리기 (TODO: 실제 아이콘 이미지 사용)
                // canvas.drawBitmap(getShapeIcon(synergy.getShapeType()), itemStartX, currentY, null);

                canvas.drawText(displayedText, textX, currentY + itemHeight / 2 + getTextBaselineOffset(synergyItemPaint), synergyItemPaint); // 기준선 조정

                // 활성화된 티어 표시 (예: 별과 조건)
                String tierText = "★".repeat(synergy.getTier()) + " (" + "TODO" + "/" + "TODO" + ")"; // TODO: 현재 유닛 수 및 다음 조건 값 사용
                float tierTextX = textX + synergyItemPaint.measureText(displayedText) + Metrics.GRID_UNIT * (0.2f);
                // 별과 조건 텍스트도 잘라내기
                float maxTierTextWidth = panelRect.right - tierTextX - Metrics.GRID_UNIT * (0.2f); // 남은 공간
                if (activeTierPaint.measureText(tierText) > maxTierTextWidth) {
                    int numChars = activeTierPaint.breakText(tierText, true, maxTierTextWidth, null);
                    tierText = tierText.substring(0, numChars) + "...";
                }

                canvas.drawText(tierText, tierTextX, currentY + itemHeight / 2 + getTextBaselineOffset(activeTierPaint), activeTierPaint);


                currentY += itemHeight + Metrics.GRID_UNIT * (0.1f); // 다음 항목 Y 위치
            }
        }

        currentY += Metrics.GRID_UNIT * (0.5f); // 섹션 간 간격

        // ColorType 섹션 제목
        sectionTitlePaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("색상 시너지", itemStartX, currentY + sectionTitlePaint.getTextSize() / 2, sectionTitlePaint);
        currentY += Metrics.GRID_UNIT * (0.6f); // 다음 요소 Y 위치

        synergyItemPaint.setTextAlign(Paint.Align.LEFT);
        activeTierPaint.setTextAlign(Paint.Align.LEFT);


        // ColorType 시너지 항목 그리기
        for (SynergyFactory.ActiveSynergy synergy : activeSynergies) {
            if (synergy.getColorType() != null) {
                String text = getSynergyText(synergy);
                float textX = itemStartX + Metrics.GRID_UNIT * (0.8f); // 아이콘 공간 확보
                float maxTextWidth = panelRect.right - textX - Metrics.GRID_UNIT * (0.2f) - activeTierPaint.measureText("★".repeat(synergy.getTier()) + " (99/99)"); // 텍스트 최대 너비

                // 텍스트가 패널 너비를 벗어나는지 확인하고 잘라내기
                String displayedText = text;
                if (synergyItemPaint.measureText(text) > maxTextWidth) {
                    int numChars = synergyItemPaint.breakText(text, true, maxTextWidth, null);
                    displayedText = text.substring(0, numChars) + "...";
                }


                // 아이콘 그리기 (TODO: 실제 아이콘 이미지 사용)
                // canvas.drawRect(itemStartX, currentY, itemStartX + Metrics.gridToPixel(0.6f), currentY + itemHeight, getColorPaint(synergy.getColorType())); // 색상 사각형 예시

                canvas.drawText(displayedText, textX, currentY + itemHeight / 2 + getTextBaselineOffset(synergyItemPaint), synergyItemPaint);

                // 활성화된 티어 표시
                String tierText = "★".repeat(synergy.getTier()) + " (" + "TODO" + "/" + "TODO" + ")"; // TODO: 현재 유닛 수 및 다음 조건 값 사용
                float tierTextX = textX + synergyItemPaint.measureText(displayedText) + Metrics.GRID_UNIT * (0.2f);
                // 별과 조건 텍스트도 잘라내기
                float maxTierTextWidth = panelRect.right - tierTextX - Metrics.GRID_UNIT * (0.2f); // 남은 공간
                if (activeTierPaint.measureText(tierText) > maxTierTextWidth) {
                    int numChars = activeTierPaint.breakText(tierText, true, maxTierTextWidth, null);
                    tierText = tierText.substring(0, numChars) + "...";
                }

                canvas.drawText(tierText, tierTextX, currentY + itemHeight / 2 + getTextBaselineOffset(activeTierPaint), activeTierPaint);

                currentY += itemHeight + Metrics.GRID_UNIT * (0.1f); // 다음 항목 Y 위치
            }
        }


        // 상세 정보 팝업 그리기
        if (showDetailPopup && selectedSynergy != null) {
            drawDetailPopup(canvas, selectedSynergy);
        }
    }

    // 시너지 정보 텍스트 생성 헬퍼
    private String getSynergyText(SynergyFactory.ActiveSynergy synergy) {
        String name = "";
        if (synergy.getShapeType() != null) {
            name = synergy.getShapeType().toString(); // ShapeType 이름 사용
        } else if (synergy.getColorType() != null) {
            name = synergy.getColorType().toString(); // ColorType 이름 사용
        }

        return name + " " + synergy.getTier() + "단계";
    }

    // 텍스트 세로 중앙 정렬을 위한 기준선 오프셋 계산 헬퍼
    private float getTextBaselineOffset(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return -(fm.ascent + fm.descent) / 2;
    }

    // 활성화된 티어 표시 (별 그리기) 메소드 시그니처 변경 및 내용 수정
    private void drawActiveTier(Canvas canvas, float x, float y, SynergyFactory.ActiveSynergy synergy) {
        // 이 메소드는 더 이상 사용되지 않고 draw 메소드 내에서 직접 그립니다.
        // 위 draw 메소드 수정 내용을 참고하세요.
    }


    // 상세 정보 팝업 그리기
    private void drawDetailPopup(Canvas canvas, SynergyFactory.ActiveSynergy synergy) {
        canvas.drawRoundRect(detailPopupRect, 20, 20, detailPopupBackgroundPaint); // 둥근 모서리 배경

        float textStartX = detailPopupRect.left + Metrics.GRID_UNIT * (0.2f);
        float textStartY = detailPopupRect.top + Metrics.GRID_UNIT * (0.4f); // 제목 아래 여백
        float lineHeight = Metrics.GRID_UNIT * (0.4f);

        // 시너지 이름 및 단계 (팝업 제목)
        detailPopupTextPaint.setTextAlign(Paint.Align.LEFT); // 팝업 텍스트 좌측 정렬
        detailPopupTextPaint.setColor(Color.YELLOW); // 제목 색상 변경 (예시)
        canvas.drawText(getSynergyText(synergy), textStartX, textStartY + getTextBaselineOffset(detailPopupTextPaint), detailPopupTextPaint); // 기준선 조정
        textStartY += lineHeight * 1.5f; // 제목 다음 간격

        detailPopupTextPaint.setColor(Color.WHITE); // 효과 설명 색상 복원
        canvas.drawText("효과:", textStartX, textStartY + getTextBaselineOffset(detailPopupTextPaint), detailPopupTextPaint); // 기준선 조정
        textStartY += lineHeight;


        // --- 상세 효과 설명 표시 로직 개선 ---
        // SynergyFactory에서 해당 시너지 타입 및 단계의 구체적인 효과 설명을 가져와서 표시
        // TODO: SynergyFactory에 getSynergyEffects(ShapeType, ColorType, tier) 메소드를 추가하고 사용해야 합니다.
        // 현재는 ActiveSynergy가 가진 단일 효과 정보만 표시하지만, 실제로는 해당 티어의 모든 효과를 표시해야 합니다.

        // 예시: (SynergyFactory에 getSynergyEffects 메소드가 있다고 가정)
        // List<BattleUnit.SynergyEffect> effects = SynergyFactory.getSynergyEffects(synergy.getShapeType(), synergy.getColorType(), synergy.getTier());
        // if (effects != null) {
        //     for (BattleUnit.SynergyEffect effect : effects) {
        //         String effectDescription = getEffectDescription(effect);
        //         // 텍스트가 팝업 너비를 벗어나지 않도록 줄바꿈 또는 잘라내기
        //         float maxEffectTextWidth = detailPopupRect.width() - Metrics.GRID_UNIT * (0.4f); // 팝업 내부 너비 (좌우 여백 제외)
        //         List<String> lines = splitTextIntoLines(effectDescription, maxEffectTextWidth, detailPopupTextPaint); // 줄바꿈 헬퍼 메소드 필요
        //         for (String line : lines) {
        //             canvas.drawText("- " + line, textStartX + Metrics.GRID_UNIT * 0.2f, textStartY + getTextBaselineOffset(detailPopupTextPaint), detailPopupTextPaint); // 들여쓰기 및 기준선 조정
        //             textStartY += lineHeight;
        //         }
        //     }
        // }


        // 임시 효과 표시 (ActiveSynergy가 가진 단일 효과 정보만 표시)
        if (synergy.getEffect() != null) {
            String effectDescription = getEffectDescription(synergy.getEffect());
            // 텍스트가 팝업 너비를 벗어나지 않도록 잘라내기
            float maxEffectTextWidth = detailPopupRect.width() - Metrics.GRID_UNIT * (0.4f); // 팝업 내부 너비 (좌우 여백 제외)
            String displayedEffectText = effectDescription;
            if (detailPopupTextPaint.measureText(effectDescription) > maxEffectTextWidth) {
                int numChars = detailPopupTextPaint.breakText(effectDescription, true, maxEffectTextWidth, null);
                displayedEffectText = effectDescription.substring(0, numChars) + "...";
            }

            canvas.drawText("- " + displayedEffectText, textStartX + Metrics.GRID_UNIT * 0.2f, textStartY + getTextBaselineOffset(detailPopupTextPaint), detailPopupTextPaint); // 들여쓰기 및 기준선 조정
            textStartY += lineHeight;
        }


        // TODO: 다음 시너지 단계 정보 표시 (필요시)
        // 이 부분도 SynergyFactory에서 데이터 가져와서 줄바꿈/잘라내기 적용하여 표시
    }

    // 텍스트를 주어진 너비에 맞춰 여러 줄로 나누는 헬퍼 메소드 (구현 필요)
    // private List<String> splitTextIntoLines(String text, float maxWidth, Paint paint) { ... }


    // 효과 타입에 따른 설명 문자열 반환 헬퍼 (SynergyFactory에 구현하는 것이 더 적합)
    @SuppressLint("DefaultLocale")
    private String getEffectDescription(BattleUnit.SynergyEffect effect) {
        // TODO: SynergyEffect 타입에 따른 구체적인 한글 설명 반환 로직 구현 (이전 제안과 동일)
        switch (effect.getType()) {
            case ATTACK_BONUS: return "공격력 +" + (int)effect.getValue();
            case DEFENSE_BONUS: return "방어력 +" + (int)effect.getValue();
            case MAX_HP_BONUS: return "최대 체력 +" + (int)effect.getValue();
            case ATTACK_SPEED_BONUS: return "공격 속도 " + (effect.getValue() > 0 ? "+" : "") + String.format("%.1f", effect.getValue() * 100) + "%";
            case ATTACK_RANGE_BONUS: return "공격 범위 +" + String.format("%.1f", effect.getValue()); // 픽셀 값을 그리드 단위로 변환
            case AREA_RANGE_BONUS: return "범위 공격 범위 +" + String.format("%.1f", effect.getValue());
            case SPEED_BONUS: return "이동 속도 +" + (int)effect.getValue();
            // TODO: 다른 효과 타입에 대한 설명 추가
            default: return effect.getType().toString();
        }
    }

    @Override
    public boolean isVisible(GameState state) {
        // GameState는 필요없으므로 isVisible 변수만 반환
        return isVisible;
    }

    public void open() {
        isVisible = true;
        setOpenerButtonVisibility(false);
    }

    public void addOpener(UiManager.Button button){
        openerButton = button;
    }

    private void setOpenerButtonVisibility(boolean visibility){
        if (openerButton != null) {
            GameState[] visibilityStates = {GameState.PREPARE, GameState.BATTLE, GameState.RESULT};
            for (GameState state : visibilityStates)
                openerButton.setVisibility(state, visibility);
        }
    }
}