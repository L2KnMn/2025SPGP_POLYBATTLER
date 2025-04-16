# 2025SPGP 
## Poly Battler
2025년 스마트폰게임프로그래밍 수업 텀프로젝트 개발 게임\
by 게임공학과 이관민

![Lobby Image](https://github.com/user-attachments/assets/d73ca04d-e420-4f11-a413-d50f8aa1418d)

**게임 컨셉: 도형을 이용해 표현한 캐릭터, 오토배틀러 장르식 게임 진행**

환경
  - 세로 환경에서 게임을 한다고 가정
  - landscape로 휴대폰이 바뀌어도 화면이 그대로 portrait로 유지된다고 가정한다
  - 한손으로 엄지만으로 조작하기 쉬운 게임을 만든다

---

예상 게임 실행 흐름

게임 흐름도
![흐름도](https://github.com/user-attachments/assets/0808535a-b2dc-4766-8ecc-43756df99e80)

---

개발 범위

로비 
  - 대문
  - 게임 시작
  - 전적 기록 : 이전 게임 기록을 저장해뒀다가 불러오기
    - 로비 액티비티에서 Custom View를 통해 기록(언제, 몇 라운드까지 갔는지, 마지막 단계 사용한 조합)을 윈도우로 보여주기
    - 기록 삭제 기능

게임 진행 
  - 10라운드 정도 진행하면서 덱 빌딩
    - 10라운드 완료 되면, 게임 클리어 UI 보여주고 마무리
    - 모든 캐릭터 사망하면 그 전 라운드까지 클리어 기록으로 인정하고 기록 보여줌
    - 유저가 결과를 확인하면 데이터 저장하고 게임 액티비티를 정리하고 로비로 이동함
  - 플레이어 캐릭터
    - 별도로 구현할 예정 없음
    - 터치 조작을 위한 커서나 터치 이펙트 정도 구현할 가능성 있음
  - 게임 캐릭터 동그라미, 네모, 세모 등의 간단한 도형 및 빨간색, 파란색, 초록색 등 색으로 캐릭터 표현, 한 번에 최대 5개의 캐릭터가 파티로 전투 가능하도록 함
    - 동그라미 : 원거리 공격 캐릭터, 낮은 공격력, 범위 공격, 낮은 체력
    - 네모 : 근거리 공격 캐릭터, 높은 공격력, 단일 공격, 높은 체력
    - 세모 : 원거리 공격 캐릭터, 높은 공격력, 단일 공격, 낮은 체력
    - 빨간색, 파란색, 초록색 세 가지 색상 간에 시너지 2 / 4 / 5 단위 버프
    - 도형 숫자 별로 버프 2 / 3 / 4 / 5 단위 버프
      - 캐릭터는 도형마다 다르게 행동하므로, Polyman class를 상속 받은 별도의 3개의 클래스로 구분하고, 색깔은 속성으로만 구분해서 표현할 예정
  - 밴치 : 5개까지 상점에서 산 캐릭터를 저장해뒀다가 맵으로 내볼 수 있도록 함
  - 진형 시너지 : 캐릭터 도형, 색 별로 같은 것들이 모일 때마다 간단한 버프 요소
    - 기본적인 전투 시스템의 뼈대가 만들어지면 버프 효과 같은 것을 얹어서 만들 생각임
  - 상점 : 게임 진행 중 매 라운드마다 종료 후 상점 이용 가능
    - 상점에서 한 번에 3개식 캐릭터를 표시해서 구매 가능
    - 이미 있는 캐릭터 판매 -> 별도로 판매 창을 만들지 끌어다가 넣을지 
    - 리롤 기능
  - 조합 : 같은 색, 도형 캐릭터를 3개가 모이면 자동 레벨업 -> 2레벨까지 표현
    - 2레벨 캐릭터는 별도의 캐릭터보단 도형 별로 구분한 클래스 내에서 레벨에 따라 다르게 그리는 식으로 표현할 생각임
    - 현재 생각 중인 방식은 레벨에 따라 캐릭터 주위에 뭔가 이펙트가 추가되는 식 이미 크기는 한 칸에 딱 맞게 그리기 때문에 
  - 맵 : 사각형으로 7 * 4 정도 크기로 구현 플레이어는 아래 편에 3 * 4 사이즈 맵에 자기 캐릭터를 배치해서 진형을 만들도록 하고 자동 전투 진행
    - 배치할 수 없는 부분은 가시성을 위해 좀 더 어둡게 필터를 씌워서 구분감 있도록
    - 배치할 때 배치 예상 되는 부분 표시하는 기능 -> 구현 완료
      - 만일 배치할 수 없는 곳에 가져가면 자동으로 적절한 위치 혹은 이전 초기 위치 등으로 예상 배치 이펙트가 옮겨감
      - 밴치에서 5명 이상 끌어오거나, 하면 마찬가지로 밴치로 예상 이동 지점 표시하고 이동 불가능하게 처리할 예정
    - 맵과 밴치는 기능적으로 비슷한 점이 있음
      - 다만, 같은 클래스를 이용해서 구현하거나, 상속 하기에도 차이점 존재
      - 따로 클래스를 만들기보단 맵 내에 밴치를 추가해서 별도 처리하는 방식으로 할 생각임
      - 맵과 밴치 모두 오가면서 표시할 수 있게 현재는 맵 내에 예상 배치 기능이 포함되어 있지만 분리해서 따로 커서 클래스 구현할 생각
  - 에너미 : 똑같이 동그라미 세모 네모 등의 도형 등장, 검은색 필터를 씌워서 구분감 있도록 할 생각임. 엘리트 에너미는 아직 기획 없음.
    - 적의 숫자는 제한 없음 한 번에 최대 3 * 4 숫자가 등장 가능
    - 웨이브 별도 고려 안 함 -> 한 번에 다 등장 
    - 적의 조합 패턴은 라운드 별로 미리 생성해서 json이나 xml로 저장해뒀다가 사용함
    - 적의 조합에 추가로 이전 라운드 클리어 기록을 가져와서 등장하도록 함
    - 별도의 클래스로 구현하지 않고 IEnemy 인터페이스를 만들어서 구분할 생각임 

  - 게임 정산
    - 게임 오버 혹은 10라운드까지 게임 클리어 -> UI로 결과 보여주기
    - 터치 등으로 확인 하면 로비로 돌아가면서 덱을 점수와 함께 기록 -> 이 기록된 덱은 후에 다음 게임에 활용 및 점수 랭킹으로 확인
   
  - 게임 프레임워크
    - 수업 시간에 진행한 프레임워크에 필요한 클래스를 추가로 구현해서 사용 중
      - IGameObject로 게임 오브젝트를 구분함
      - Transform 클래스를 만들어서 게임뷰 내에서 그려질 인스턴스 뿐 아니라 이펙트처럼 크기, 좌표 등을 가지는 모든 객체가 이 클래스를 가지고 상호작용하고, 파라미터로 넘겨줘서 사용함
        - IGameObject는 반드시 getTransform을 가지므로 모든 IGameObject를 상속 받는 게임 오브젝트들은 Transform을 구현하도록 함
        - Rigidbody 변수를 가지고 캐릭터처럼 한 칸에 하나씩만 존재하는 것과 겹쳐도 상관 없는 것을 구분
        - 충돌 계산 등도 이 클래스 내부에 구현해서 처리
      
---

게임의 레퍼런스

![레퍼런스](https://github.com/user-attachments/assets/0224e5c9-578c-454d-abc8-ff32e522e931)

![image](https://github.com/user-attachments/assets/f966dbb1-ae6a-4701-b3ba-6e972059cc56)

게임 전투 진행 컨셉 이미지

![image](https://github.com/user-attachments/assets/8f545e94-d683-4f98-84a7-06d5c6786792)

![image](https://github.com/user-attachments/assets/915135a6-4bf0-4f97-af23-de1fed1f381e)

게임 상점 컨셉 이미지

---

*개발 일정*\
~4월 2주차 : 로비, 메인 게임 이동 및 게임 종료 후 로비로 돌아오 기능, 메인 게임에 맵 및 밴치에 캐릭터 배치~=>완료

4월 3주차 : 상점 기능, 전투 단계 진입(Scene 내의 상태 전환), 에너미 캐릭터 생성, 전투 기능(이동, 공격, HP 등) 구현

4월 4주차 : 에너미 캐릭터 및 AI 구현, 10단계 라운드 동안 적 캐릭터 생성, 전투 등의 루프 구현

4월 5주(5월 1주차): UI 및 전투 결과 데이터 저장 및 불러오기 구현

5월 2주차 : 진형 시너지, 2레벨 조합 및 레벨 디자인(상점 구매 가격, 새로 고침 가격, 라운드 별 보상), 

5월 3주차 : 애니메이션 등의 퀄리티 및 이펙트 등 시각 효과 향상

5월 4주차 : 게임 사운드, 버그 수정 및 완성도 향상

---
