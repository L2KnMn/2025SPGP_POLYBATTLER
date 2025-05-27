package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map; // Map import

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.BattleController;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree.BattleUnit;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.SynergyFactory;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

// Synergy 정보를 표시하는 내부 클래스
public class SynergyDisplay implements IGameObject, IEventHandle {
    private List<SynergyFactory.ActiveSynergy> activeSynergies = new ArrayList<>();
    // 현재 유닛 수를 저장 (ActiveSynergy에 count가 있다면 이 Map은 필요 없음)
    // private Map<Polyman.ShapeType, Integer> shapeCounts = new HashMap<>();
    // private Map<Polyman.ColorType, Integer> colorCounts = new HashMap<>();

    private final Transform transform; // 패널 자체의 위치와 크기
    private final Paint panelBackgroundPaint; // 패널 배경 Paint
    private final Paint sectionTitlePaint; // 섹션 제목 Paint
    private final Paint synergyItemPaint; // 시너지 항목 텍스트 Paint (버프 이름, 단계)
    private final Paint synergyEffectPaint; // 상세 버프 효과 텍스트 Paint
    private final Paint activeTierPaint; // 활성화된 티어 표시 Paint (별, 조건)

    private boolean isVisible = false; // 패널 가시성

    private UiManager.Button openerButton; // 시너지 패널을 연 버튼

    public SynergyDisplay(float x, float y, float width, float height) {
        transform = new Transform(this, x, y);
        transform.setSize(width, height);

        panelBackgroundPaint = new Paint();
        panelBackgroundPaint.setColor(Color.argb(180, 50, 50, 50)); // 반투명 어두운 회색 배경

        sectionTitlePaint = new Paint();
        sectionTitlePaint.setTextSize(Metrics.GRID_UNIT * 0.5f); // 섹션 제목 크기
        sectionTitlePaint.setColor(Color.YELLOW); // 섹션 제목 색상
        sectionTitlePaint.setTextAlign(Paint.Align.LEFT); // 섹션 제목 좌측 정렬


        synergyItemPaint = new Paint();
        synergyItemPaint.setTextSize(Metrics.GRID_UNIT * (0.4f)); // 시너지 항목 텍스트 크기
        synergyItemPaint.setColor(Color.WHITE); // 시너지 항목 텍스트 색상
        synergyItemPaint.setTextAlign(Paint.Align.LEFT); // 시너지 항목 텍스트 좌측 정렬

        synergyEffectPaint = new Paint();
        synergyEffectPaint.setTextSize(Metrics.GRID_UNIT * (0.35f)); // 상세 버프 효과 텍스트 크기
        synergyEffectPaint.setColor(Color.LTGRAY); // 상세 버프 효과 텍스트 색상 (회색)
        synergyEffectPaint.setTextAlign(Paint.Align.LEFT); // 상세 버프 효과 텍스트 좌측 정렬


        activeTierPaint = new Paint();
        activeTierPaint.setTextSize(Metrics.GRID_UNIT * (0.4f));
        activeTierPaint.setColor(Color.CYAN); // 활성화된 티어 표시 색상 (예: 별 색상)
        activeTierPaint.setTextAlign(Paint.Align.LEFT);
    }

    // 시너지 데이터 업데이트 (ActiveSynergy에 count와 condition이 있다고 가정)
    public void updateSynergies(List<SynergyFactory.ActiveSynergy> synergies) {
        this.activeSynergies = synergies;
    }

    // 패널 가시성 설정
    public void setVisibility(boolean visible) {
        this.isVisible = visible;
    }

    // 터치 이벤트 처리 (상세 팝업 제거로 인해 간소화)
    @Override
    public boolean handleTouchEvent(MotionEvent event, float x, float y) {
        if (!isVisible) return false; // 패널이 보이지 않으면 터치 처리 안 함

        RectF panelRect = transform.getRect();

        // 패널 영역 밖 터치 처리
        if (!panelRect.contains(x, y)) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // 상태 초기화
                isVisible = false;
                // 버튼 다시 활성화
                setOpenerButtonVisibility(true);
                Log.d("SynergyDisplay", "Synergy Panel Closed");
            }
        }
        return true;
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
        float itemContentWidth = panelRect.width() - Metrics.GRID_UNIT * (0.4f); // 항목 내용 너비 (좌우 여백 제외)
        float itemLineHeight = Metrics.GRID_UNIT * 0.4f; // 기본 줄 높이 (synergyItemPaint 텍스트 크기 기반)

        // ShapeType 섹션 제목
        canvas.drawText("모양 시너지", itemStartX, currentY + sectionTitlePaint.getTextSize() / 2 + getTextBaselineOffset(sectionTitlePaint), sectionTitlePaint);
        currentY += Metrics.GRID_UNIT * (0.6f); // 다음 요소 Y 위치

        // ShapeType 시너지 항목 그리기
        for (SynergyFactory.ActiveSynergy synergy : activeSynergies) {
            if (synergy.getShapeType() != null) {
                float itemSectionCurrentY = currentY; // 각 시너지 항목 섹션의 시작 Y

                // 1. 버프 이름 출력 (예: "사각형 시너지")
                String nameText = getSynergyName(synergy); // 시너지 이름만 가져오는 헬퍼
                canvas.drawText(nameText, itemStartX, itemSectionCurrentY + getTextBaselineOffset(synergyItemPaint), synergyItemPaint);
                itemSectionCurrentY += itemLineHeight; // 다음 줄로 이동

                // 2. Tier (별) 및 조건 출력 (예: "★★ (2/4)")
                // ActiveSynergy에 count와 condition이 있다고 가정합니다.
                String tierConditionText = "★".repeat(synergy.getTier()) + " (" + synergy.getCount() + "/" + synergy.getCondition() + ")";
                canvas.drawText(tierConditionText, itemStartX, itemSectionCurrentY + getTextBaselineOffset(activeTierPaint), activeTierPaint);
                itemSectionCurrentY += itemLineHeight; // 다음 줄로 이동

                // 3. 상세 버프 설명 출력 (해당 티어의 모든 효과를 가져와 표시)
                // TODO: SynergyFactory에서 해당 시너지 타입, 색상, 티어의 모든 효과 목록을 가져와야 합니다.
                List<BattleUnit.SynergyEffect> effectsToDisplay = new ArrayList<>();
                effectsToDisplay.add(synergy.getEffect());

                for (BattleUnit.SynergyEffect effect : effectsToDisplay) {
                    String effectDescription = getEffectDescription(effect);
                    List<String> lines = splitTextIntoLines(effectDescription, itemContentWidth - Metrics.GRID_UNIT * 0.2f, synergyEffectPaint); // 들여쓰기 공간 제외
                    for (String line : lines) {
                        canvas.drawText("- " + line, itemStartX + Metrics.GRID_UNIT * 0.2f, itemSectionCurrentY + getTextBaselineOffset(synergyEffectPaint), synergyEffectPaint); // 들여쓰기
                        itemSectionCurrentY += synergyEffectPaint.getTextSize() * 1.1f; // 다음 줄로 이동
                    }
                }
                currentY = itemSectionCurrentY + Metrics.GRID_UNIT * 0.1f; // 다음 시너지 항목 시작 Y (여백 추가)
            }
        }

        currentY += Metrics.GRID_UNIT * (0.5f); // 섹션 간 간격

        // ColorType 섹션 제목
        canvas.drawText("색상 시너지", itemStartX, currentY + sectionTitlePaint.getTextSize() / 2 + getTextBaselineOffset(sectionTitlePaint), sectionTitlePaint);
        currentY += Metrics.GRID_UNIT * (0.6f); // 다음 요소 Y 위치

        // ColorType 시너지 항목 그리기
        for (SynergyFactory.ActiveSynergy synergy : activeSynergies) {
            if (synergy.getColorType() != null) {
                float itemSectionCurrentY = currentY; // 각 시너지 항목 섹션의 시작 Y

                // 1. 버프 이름 출력
                String nameText = getSynergyName(synergy);
                canvas.drawText(nameText, itemStartX, itemSectionCurrentY + getTextBaselineOffset(synergyItemPaint), synergyItemPaint);
                itemSectionCurrentY += itemLineHeight; // 다음 줄로 이동

                // 2. Tier (별) 및 조건 출력
                // ActiveSynergy에 count와 condition이 있다고 가정합니다.
                String tierConditionText = "★".repeat(synergy.getTier()) + " (" + synergy.getCount() + "/" + synergy.getCondition() + ")";
                canvas.drawText(tierConditionText, itemStartX, itemSectionCurrentY + getTextBaselineOffset(activeTierPaint), activeTierPaint);
                itemSectionCurrentY += itemLineHeight; // 다음 줄로 이동

                // 3. 상세 버프 설명 출력
                List<BattleUnit.SynergyEffect> effectsToDisplay = new ArrayList<>();
                effectsToDisplay.add(synergy.getEffect());

                for (BattleUnit.SynergyEffect effect : effectsToDisplay) {
                    String effectDescription = getEffectDescription(effect);
                    List<String> lines = splitTextIntoLines(effectDescription, itemContentWidth - Metrics.GRID_UNIT * 0.2f, synergyEffectPaint); // 들여쓰기 공간 제외
                    for (String line : lines) {
                        canvas.drawText("- " + line, itemStartX + Metrics.GRID_UNIT * 0.2f, itemSectionCurrentY + getTextBaselineOffset(synergyEffectPaint), synergyEffectPaint); // 들여쓰기
                        itemSectionCurrentY += synergyEffectPaint.getTextSize() * 1.1f; // 다음 줄로 이동
                    }
                }
                currentY = itemSectionCurrentY + Metrics.GRID_UNIT * 0.1f; // 다음 시너지 항목 시작 Y (여백 추가)
            }
        }
    }

    // 텍스트를 주어진 너비에 맞춰 여러 줄로 나누는 헬퍼 메소드
    private List<String> splitTextIntoLines(String text, float maxWidth, Paint paint) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }

        int start = 0;
        while (start < text.length()) {
            int end = paint.breakText(text, start, text.length(), true, maxWidth, null);
            if (end == 0 && text.length() > start) { // 한 글자도 들어가지 않는 경우 (매우 좁은 폭)
                end = 1; // 최소 한 글자는 포함 (무한 루프 방지)
            }
            lines.add(text.substring(start, start + end));
            start += end;
        }
        return lines;
    }

    // 시너지 이름만 가져오는 헬퍼 (예: "사각형 시너지")
    private String getSynergyName(SynergyFactory.ActiveSynergy synergy) {
        String name = "";
        if (synergy.getShapeType() != null) {
            // TODO: ShapeType 열거형에 한글 이름 매핑 필요 (예: "사각형")
            name = synergy.getShapeType().toString();
        } else if (synergy.getColorType() != null) {
            // TODO: ColorType 열거형에 한글 이름 매핑 필요 (예: "빨강")
            name = synergy.getColorType().toString();
        }
        return name + " 시너지";
    }

    // 효과 타입에 따른 설명 문자열 반환 헬퍼 (Metrics.GRID_UNIT 사용)
    private String getEffectDescription(BattleUnit.SynergyEffect effect) {
        String team = effect.applicateTeam() == BattleController.Team.PLAYER ? "아군 " : "적군 ";
        String effectText = "";
        switch (effect.getType()) {
            case ATTACK_BONUS: effectText = "공격력 +" + (int)effect.getValue();
                break;
            case DEFENSE_BONUS: effectText = "방어력 +" + (int)effect.getValue();
                break;
            case MAX_HP_BONUS: effectText = "최대 체력 +" + (int)effect.getValue();
                break;
            case ATTACK_SPEED_BONUS: effectText = "공격 속도 " + (effect.getValue() > 0 ? "+" : "") + String.format("%.1f", effect.getValue() * 100) + "%";
                break;
            // Metrics.GRID_UNIT을 직접 곱하여 그리드 단위로 환산된 값을 표시
            case ATTACK_RANGE_BONUS: effectText = "공격 범위 +" + String.format("%.1f", effect.getValue() * Metrics.GRID_UNIT);
                break;
            case AREA_RANGE_BONUS: effectText = "범위 공격 범위 +" + String.format("%.1f", effect.getValue() * Metrics.GRID_UNIT);
                break;
            case SPEED_BONUS: effectText = "이동 속도 +" + (int)effect.getValue();
                break;
            default: effectText = effect.getType().toString();
        }
        effectText += effect.getValue() > 0 ? " 증가" : " 감소";
        return team + effectText;
    }

    // 텍스트 세로 중앙 정렬을 위한 기준선 오프셋 계산 헬퍼
    private float getTextBaselineOffset(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return -(fm.ascent + fm.descent) / 2;
    }

    @Override
    public boolean isVisible(GameState state) {
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
        openerButton.setVisibility(GameState.PREPARE, visibility);
        openerButton.setVisibility(GameState.BATTLE, visibility);
        openerButton.setVisibility(GameState.RESULT, visibility);
    }
}