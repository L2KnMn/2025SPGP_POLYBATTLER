# 2025SPGP 
## Poly Battler
2025년 스마트폰게임프로그래밍 수업 텀프로젝트 개발 게임

![Lobby Image](https://github.com/user-attachments/assets/d73ca04d-e420-4f11-a413-d50f8aa1418d)

**게임 컨셉: 도형을 이용해 표현한 캐릭터, 오토배틀러 장르식 게임 진행**
---
개발 범위
로비 
- 대문
- 게임 시작
- 점수 랭킹 : 이전 게임 기록을 저장해뒀다가 불러오기
게임 진행 
  - 10라운드 정도 진행하면서 덱 빌딩 
  - 게임 캐릭터 동그라미, 네모, 세모 등의 간단한 도형 및 빨간색, 파란색, 초록색 등 색으로 캐릭터 표현, 한 번에 최대 5개의 캐릭터가 파티로 전투 가능하도록 함
  - 밴치 : 5개까지 상점에서 산 캐릭터를 저장해뒀다가 맵으로 내볼 수 있도록 하
  - 진형 시너지 : 캐릭터 도형, 색 별로 같은 것들이 모일 때마다 간단한 버프 요소
  - 상점 : 게임 진행 중 매 라운드마다 종료 후 상점 이용 가능 -> 상점에서 캐릭터 구매 
  - 조합 : 같은 색, 도형 캐릭터를 3개 모으면 레벨업 -> 2레벨까지 표현 
  - 맵 : 사각형으로 4x7 정도 크기로 구현 플레이어는 4x3 사이즈 맵. 자기 캐릭터를 배치해서 진형을 만들도록 함.
게임 정산
- 게임 오버 혹은 10라운드까지 게임 클리어한 덱을 점수와 함께 기록 -> 이 기록된 덱은 후에 다음 게임에 활용 및 점수 랭킹으로 확인
---
예상 게임 실행 흐름

게임 흐름도
![SDL Diagram for Process Game](https://github.com/user-attachments/assets/78fab771-6938-4889-923f-0beed9d60088)

게임의 컨셉 그림

![image](https://github.com/user-attachments/assets/f966dbb1-ae6a-4701-b3ba-6e972059cc56)
---
개발 일정
4월 2주차 : 로비, 메인 게임 이동 및 게임 종료 후 로비로 돌아오 기능, 메인 게임에 맵 및 밴치에 캐릭터 배치
4월 3주차 : 상점 기능, 전투 기능(이동, 공격, HP 등) 구현
4월 4주차 : 에너미 캐릭터 및 AI 구현, 10단계 라운드 동안 적 캐릭터 생성, 전투 등의 루프 구현
4월 5주(5월 1주차): 전투 결과 데이터 저장 및 불러오기 구현
5월 2주차 : 진형 시너지, 2레벨 조합 및 레벨 디자인(상점 구매 가격, 새로 고침 가격, 라운드 별 보상)
5월 3주차 : 애니메이션 등의 퀄리티 및 게임 시각 효과 향상 
5월 4주차 : 게임 사운드, 버그 수정 및 완성도 향상
---
