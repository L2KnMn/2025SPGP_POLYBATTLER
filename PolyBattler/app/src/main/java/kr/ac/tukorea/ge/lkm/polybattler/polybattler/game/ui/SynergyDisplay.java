package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Layer;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree.BattleUnit;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.SynergyFactory;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
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

        // 상세 정보 팝업이 켜져 있으면 팝업 터치만 처리
        if (showDetailPopup) {
            Log.d("SynergyDisplay", "showDetailPopup is true");
            if (detailPopupRect.contains(x, y)) {
                // 팝업 내부 터치는 소비하지 않고 그대로 둠 (필요에 따라 버튼 등 추가 가능)
            } else {
                // 팝업 외부 터치는 팝업 닫기
                showDetailPopup = false;
                selectedSynergy = null;
                return true; // 팝업 닫기 이벤트를 소비
            }
            return true; // 팝업이 켜져 있을 때 다른 UI 요소로 이벤트 전달 막기
        }

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

        // 시너지 항목별 터치 영역 계산 및 처리
        float currentY = panelRect.top + Metrics.GRID_UNIT * (0.5f); // 시작 Y 위치

        // ShapeType 섹션
        currentY += Metrics.GRID_UNIT * (0.6f); // 섹션 제목 위치
        currentY += Metrics.GRID_UNIT * (0.5f); // 첫 번째 항목 시작 위치

        // TODO: 실제 시너지 항목의 터치 영역 계산 로직 구현
        // 예시: 각 시너지 항목의 영역을 계산하여 contains(x, y) 체크

        // 간략화된 예시: 활성화된 시너지 목록만 순회하며 터치 영역 체크
        float itemHeight = Metrics.GRID_UNIT * (0.5f); // 항목 높이 예시
        for (SynergyFactory.ActiveSynergy synergy : activeSynergies) {
            RectF itemRect = new RectF(panelRect.left, currentY, panelRect.right, currentY + itemHeight);
            if (itemRect.contains(x, y)) {
                // 시너지 항목 터치 시 상세 정보 팝업 표시
                selectedSynergy = synergy;
                showDetailPopup = true;
                calculateDetailPopupPosition(itemRect); // 팝업 위치 계산
                Log.d("SynergyDisplay", "Synergy Item Clicked: " + (synergy.getShapeType() != null ? synergy.getShapeType() : synergy.getColorType()) + " Tier " + synergy.getTier());
                return true; // 터치 이벤트 소비
            }
            currentY += itemHeight + Metrics.GRID_UNIT * (0.1f); // 다음 항목 Y 위치
        }


        return false; // 패널 영역 내 터치지만 시너지 항목에 해당하지 않음
    }

    // 상세 정보 팝업 위치 계산 (간단 예시)
    private void calculateDetailPopupPosition(RectF targetRect) {
        float popupWidth = Metrics.GRID_UNIT * (5); // 팝업 너비
        float popupHeight = Metrics.GRID_UNIT * (4); // 팝업 높이

        float popupX = targetRect.right + Metrics.GRID_UNIT * (0.2f); // 항목 오른쪽 옆에 표시
        float popupY = targetRect.centerY() - popupHeight / 2;

        // 화면 경계를 벗어나지 않도록 보정
        if (popupX + popupWidth > Metrics.width) {
            popupX = targetRect.left - popupWidth - Metrics.GRID_UNIT * (0.2f);
        }
        if (popupY < 0) {
            popupY = 0;
        }
        if (popupY + popupHeight > Metrics.height) {
            popupY = Metrics.height - popupHeight;
        }

        detailPopupRect.set(popupX, popupY, popupX + popupWidth, popupY + popupHeight);
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
        canvas.drawText("모양 시너지", itemStartX, currentY, sectionTitlePaint);
        currentY += Metrics.GRID_UNIT * (0.6f); // 다음 요소 Y 위치

        // TODO: 모든 ShapeType에 대해 현재 유닛 수, 진행 상황, 활성화된 티어 표시 로직 구현
        // (현재는 activeSynergies 목록만 사용하므로 활성화된 시너지들만 표시)
        for (SynergyFactory.ActiveSynergy synergy : activeSynergies) {
            if (synergy.getShapeType() != null) {
                String text = getSynergyText(synergy);
                // 아이콘 그리기 (TODO: 실제 아이콘 이미지 사용)
                // canvas.drawBitmap(getShapeIcon(synergy.getShapeType()), itemStartX, currentY - itemHeight / 2, null); // 예시

                float textX = itemStartX + Metrics.GRID_UNIT * (0.8f); // 아이콘 옆 텍스트 위치
                canvas.drawText(text, textX, currentY + itemHeight / 2, synergyItemPaint);

                // TODO: 진행 상황 바 그리기 (현재 유닛 수 정보를 받아와야 함)
                // drawProgressBar(canvas, itemX + Metrics.gridToPixel(2), currentY + itemHeight / 4, Metrics.gridToPixel(3), itemHeight / 2, currentCount, nextTierCount);

                // 활성화된 티어 표시 (예: 별 아이콘)
                drawActiveTier(canvas, textX + synergyItemPaint.measureText(text) + Metrics.GRID_UNIT * (0.2f),
                        currentY + itemHeight / 2, synergy.getTier());


                currentY += itemHeight + Metrics.GRID_UNIT * (0.1f); // 다음 항목 Y 위치
            }
        }

        currentY += Metrics.GRID_UNIT * (0.5f); // 섹션 간 간격

        // ColorType 섹션 제목
        canvas.drawText("색상 시너지", itemStartX, currentY, sectionTitlePaint);
        currentY += Metrics.GRID_UNIT * (0.6f); // 다음 요소 Y 위치

        // TODO: 모든 ColorType에 대해 현재 유닛 수, 진행 상황, 활성화된 티어 표시 로직 구현
        for (SynergyFactory.ActiveSynergy synergy : activeSynergies) {
            if (synergy.getColorType() != null) {
                String text = getSynergyText(synergy);
                // 아이콘 그리기 (TODO: 실제 아이콘 이미지 사용)
                // canvas.drawRect(itemStartX, currentY, itemStartX + Metrics.gridToPixel(0.6f), currentY + itemHeight, getColorPaint(synergy.getColorType())); // 색상 사각형 예시

                float textX = itemStartX + Metrics.GRID_UNIT * (0.8f); // 아이콘 옆 텍스트 위치
                canvas.drawText(text, textX, currentY + itemHeight / 2, synergyItemPaint);

                // TODO: 진행 상황 바 그리기 (현재 유닛 수 정보를 받아와야 함)

                // 활성화된 티어 표시
                drawActiveTier(canvas, textX + synergyItemPaint.measureText(text) + Metrics.GRID_UNIT * (0.2f),
                        currentY + itemHeight / 2, synergy.getTier());


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
        // TODO: 실제 게임에서는 ShapeType과 ColorType의 한글 이름을 사용하도록 매핑 필요

        return name + " " + synergy.getTier() + "단계";
    }

    // 활성화된 티어 표시 (예: 별 그리기)
    private void drawActiveTier(Canvas canvas, float x, float y, int tier) {
        // TODO: 별 아이콘 또는 다른 시각적 표시 구현
        String stars = "";
        for (int i = 0; i < tier; i++) {
            stars += "★"; // 별 문자 사용 예시
        }
        canvas.drawText(stars, x, y, activeTierPaint);
    }

    // 상세 정보 팝업 그리기
    private void drawDetailPopup(Canvas canvas, SynergyFactory.ActiveSynergy synergy) {
        canvas.drawRoundRect(detailPopupRect, 20, 20, detailPopupBackgroundPaint); // 둥근 모서리 배경

        float textStartX = detailPopupRect.left + Metrics.GRID_UNIT * (0.2f);
        float textStartY = detailPopupRect.top + Metrics.GRID_UNIT * (0.2f);
        float lineHeight = Metrics.GRID_UNIT * (0.4f);

        // 시너지 이름 및 단계
        canvas.drawText(getSynergyText(synergy), textStartX, textStartY, detailPopupTextPaint);
        textStartY += lineHeight;

        // TODO: 시너지 효과 설명 표시 로직 구현
        // ActiveSynergy 객체는 효과 목록(List<SynergyEffect>)을 직접 가지고 있지 않음.
        // SynergyFactory에서 해당 시너지의 모든 단계별 효과 정보를 가져와야 함.
        // 여기서는 간략하게 효과 타입을 문자열로 표시하는 예시
        canvas.drawText("효과:", textStartX, textStartY, detailPopupTextPaint);
        textStartY += lineHeight;

        // TODO: SynergyFactory에서 해당 시너지 타입 및 단계의 구체적인 효과 설명을 가져와서 표시
        // 예시:
        // List<SynergyEffect> effects = SynergyFactory.getSynergyEffects(synergy.getShapeType(), synergy.getColorType(), synergy.getTier());
        // for (SynergyEffect effect : effects) {
        //     canvas.drawText("- " + getEffectDescription(effect), textStartX, textStartY, detailPopupTextPaint);
        //     textStartY += lineHeight;
        // }

        // 임시 효과 표시 (효과 타입만 표시)
        canvas.drawText("- " + synergy.getEffect().getType().toString() + ": " + synergy.getEffect().getValue(), textStartX, textStartY, detailPopupTextPaint);
        textStartY += lineHeight;


        // TODO: 다음 시너지 단계 정보 표시 (필요시)
        // int nextTier = synergy.getTier() + 1;
        // if (SynergyFactory.hasSynergyTier(synergy.getShapeType(), synergy.getColorType(), nextTier)) {
        //     canvas.drawText("다음 단계 (" + nextTier + "):", textStartX, textStartY, detailPopupTextPaint);
        //     textStartY += lineHeight;
        //     List<SynergyEffect> nextEffects = SynergyFactory.getSynergyEffects(synergy.getShapeType(), synergy.getColorType(), nextTier);
        //     for (SynergyEffect effect : nextEffects) {
        //         canvas.drawText("- " + getEffectDescription(effect), textStartX, textStartY, detailPopupTextPaint);
        //         textStartY += lineHeight;
        //     }
        // }
    }

    // 효과 타입에 따른 설명 문자열 반환 헬퍼 (SynergyFactory에 구현하는 것이 더 적합)
    private String getEffectDescription(BattleUnit.SynergyEffect effect) {
        // TODO: SynergyEffect 타입에 따른 구체적인 한글 설명 반환 로직 구현
        switch (effect.getType()) {
            case ATTACK_BONUS: return "공격력 +" + (int)effect.getValue();
            case DEFENSE_BONUS: return "방어력 +" + (int)effect.getValue();
            case MAX_HP_BONUS: return "최대 체력 +" + (int)effect.getValue();
            case ATTACK_SPEED_BONUS: return "공격 속도 " + (effect.getValue() > 0 ? "+" : "") + String.format("%.1f", effect.getValue() * 100) + "%";
            case ATTACK_RANGE_BONUS: return "공격 범위 +" + String.format("%.1f", Metrics.GRID_UNIT * (effect.getValue())); // 픽셀 값을 그리드 단위로 변환
            case AREA_RANGE_BONUS: return "범위 공격 범위 +" + String.format("%.1f", Metrics.GRID_UNIT * (effect.getValue()));
            case SPEED_BONUS: return "이동 속도 +" + (int)effect.getValue();
            // TODO: 다른 효과 타입에 대한 설명 추가
            default: return effect.getType().toString();
        }
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