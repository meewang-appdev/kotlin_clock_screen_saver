# Repository Guidelines

## 프로젝트 구조
- 루트: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`.
- 앱 모듈 `app/`
  - 소스: `app/src/main/java/com/example/clockscreensaver/`
    - `dream/` DreamService 진입점
    - `ui/clock/` 시계 UI, 스타일 스위치, Dream 터치 제어
    - `ui/settings/` 설정 화면/미리보기
    - `ui/theme/` 컬러·타입
    - `data/` DataStore 모델/레포
  - 리소스: `app/src/main/res/` (values, xml), 매니페스트 `app/src/main/AndroidManifest.xml`
- 문서: `docs/plan.md`(PRD/TRD), `AGENTS.md`

## 기능/동작 요약
- DreamService 기반 스크린세이버. Compose 전용.
- 시계 스타일 3종: 기본/분리/미니멀. 좌·우 스와이프로 변경.
- 단일 탭(소거리·300ms 이하)은 Dream 종료. 스와이프는 유지.
- 픽셀 시프트로 번인 방지(1분마다 위치 이동). 다크 팔레트 기본.
- DataStore 설정: 24h 여부, 텍스트 색상, 번인 보호, 시계 스타일(+폰트/밝기 초기값).
- 설정 화면: 색상/스타일 선택, 24h/번인 토글, 풀스크린 미리보기, 시스템 화면보호기 설정 바로가기.

## 빌드·실행
- 빌드: `./gradlew :app:assembleDebug`
- 설치: `./gradlew :app:installDebug` 또는 `adb install -r app/build/outputs/apk/debug/app-debug.apk`
- 실행:
  - 앱 실행 → 설정에서 옵션 변경 → “전체 화면 미리보기 실행” 확인
  - 시스템 설정 > 화면보호기(꿈) > Clock Screen Saver 선택 → 미리보기/실동작에서 스와이프=스타일 변경, 탭=종료 확인

## 코딩 스타일·네이밍
- Kotlin + Jetpack Compose, XML 미사용.
- `kotlin.code.style=official`; Compose Material3/BOM 사용.
- 패키지 단위 분리(기능별). 클래스/컴포저블 PascalCase, 함수·변수 camelCase, 상수 UPPER_SNAKE_CASE.
- 문자열/색상은 리소스 또는 `ui/theme`에 상수화. 하드코딩 최소화.

## 테스트 지침
- 단위: JUnit4 (`app/src/test`), 계측/Compose: `app/src/androidTest`.
- 명명: `{ClassName}Test.kt`. 시나리오별 단일 책임.
- DataStore 테스트 시 코루틴 테스트 디스패처와 임시 DataStore 사용.

## PR/커밋 가이드
- 커밋 메시지: 현재형/간결(예: `Handle tap to exit dream`, `Add swipe style switching`).
- 리팩터와 기능 추가는 분리. PR에 요약, 테스트 로그, UI 변경 시 스크린샷/GIF 포함.
- PR 전 `./gradlew :app:assembleDebug` 통과 확인.

## 주의/기기 설정
- 매니페스트에 `android.permission.BIND_DREAM_SERVICE` 선언.
- Dream 인터랙티브 모드(`isInteractive=true`): 터치 이벤트를 소유해 스와이프/탭 분기 처리.
- OEM마다 Dream 종료 처리 다를 수 있음 → 실기기(삼성/픽셀 등) 확인 권장. 비밀정보 저장 금지.
