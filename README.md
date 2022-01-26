# springboot-reactive
Reactive programming with spring boot and mongoDB
 ( ItemRepository, CartRepository CRUD )

- spring boot
- mongoDB
- Thymeleaf
- Maven


※ Hooks.onOperatorDebug()
Enable operator stack recorder that captures a declaration stack whenever an operator is instantiated.
디버깅 모드를 활성화할 수 있으며, 이럴 경우 에러가 발생했을 때 출력되는 스택트레이스에 시작부터 에러가 났을 때까지 연산자의 목록을 모두 볼 수 있습니다.
비용 및 성능 등의 이슈가 발생할 수 있으므로, 꼭 필요한 경우가 아니고선 절대 사용하지 맙시다.

※ log()
log() 로그 수준 기본값은 Info.
두번째 인자로 로그수준 level을 넘겨주면 원하는 수준으로 출력 가능.
세번째 인자로 리액티브 스트림의 Signal을 넘겨주면 특정 신호에 대한 로그만 출력 가능.
(cartServie.addToCart())



