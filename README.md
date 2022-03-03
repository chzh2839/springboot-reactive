# springboot-reactive
Reactive programming with spring boot and mongoDB  
 ( ItemRepository, CartRepository CRUD )

- spring boot
- mongoDB
- Thymeleaf
- Maven
- Java 1.8

***
### Hooks.onOperatorDebug()  
Enable operator stack recorder that captures a declaration stack whenever an operator is instantiated.  
디버깅 모드를 활성화할 수 있으며, 이럴 경우 에러가 발생했을 때 출력되는 스택트레이스에 시작부터 에러가 났을 때까지 연산자의 목록을 모두 볼 수 있습니다.  
비용 및 성능 등의 이슈가 발생할 수 있으므로, 꼭 필요한 경우가 아니고선 절대 사용하지 맙시다.

### log()  
log() default log level is 'Info'.  
두번째 인자(second element)로 로그수준 level을 넘겨주면 원하는 수준으로 출력 가능.  
세번째 인자(third element)로 리액티브 스트림의 Signal을 넘겨주면 특정 신호에 대한 로그만 출력 가능.  
(ref. cartServie.addToCart())  

### Blockhound  
Checking tool to find any Blocking code.  
리액티브 프로그래밍은 하나의 블로킹 코드만 있어도 리액티브 플로우가 깨지고 성능이 급격히 나빠진다.  
이러한 블로킹 코드를 검출하는 도구로 '블록하운드'가 있다.  
일부 블로킹 코드를 허용할 수 있는데, 저수준의 메서드를 허용하는 것보다 구체적인 일부 지점만 허용하는 것이 안전하다.  
(** 테스트 환경에서만 사용하는 것이 낫다.)  

### Reactive Code Test  
The point is to test together with reactive stream signal.  
리액티브 코드 테스트 시, 핵심은 기능만을 검사하는 게 아니라 리액티브 스트림 시그널도 함께 검사해야 한다는 것이다.  
(onSubscribe, onNext, onError, onComplete)  

### Test Strategy
1. null 값 처리를 포함한 도메인 객체 테스트  
2. 가짜 협력자를 활용해서 모든 비즈니스 로직을 검사하는 서비스 계층 테스트  
3. 내정 웹 컨테이너를 사용하는 약간의 종단 테스트  

### Slice Test  
Middle level test between unit test and end-to-end integration test.  
단위 테스트와 종단 간 통합 테스트 중간 수준의 테스트.  

### spring rest doc  
API documentation tool.

### Spring Hateoas(스프링 헤이티오스)
스프링 헤이티오스는 하이퍼미디어에 행동유도성(affordance)을 추가한 API를 제공한다.
- RepresentationModel : 링크 정보를 포함하는 도메인 객체를 정의하는 기본 타입.
- EntityModel : 도메인 객체를 감싸고 링크를 추가할 수 있는 모델. RepresentationModel을 상속받음.
- CollectionModel : 도메인 객체 컬렉션을 감싸고 링크를 추가할 수 있는 모델. RepresentationModel을 상속받음.
- PagedModel : 페이징 관련 메타데이터를 포함하는 모델. CollectionModel을 상속받음.

### Testcontainer
( https://www.testcontainers.org/ )
- Docker를 활용하는 자바 테스트 지원 lib
- 도커에서 실행될 수만 있다면, 어떤 DB나 메시지 브로커, 서드파티 시스템도 테스트용으로 쉽게 쓸 수 있다.
- 테스트가 종료되면 테스트에 사용됐던 여러 컨테이너 자원도 남김없이 깔끔하게 종료된다. 그래서 테스트를 실행할 때마다 아주 쉽게 깨끗한 상태의 래빗엠큐를 실행하고 사용할 수 있다.

### Scheduler
- 스케줄러를 통해 개별 수행 단계가 어느 스레드에서 실행될지 지정할 수 있다.
- 한 개의 스레드만 사용하면서도 비동기 논블로킹 코드를 작성할 수 있다.
- 스레드가 시스템 자원의 가용성에 반응할 준비가 되어있을 때 개별 수행 단계를 실행하는 방식을 사용하면 가능하다.
1. Schedulers.immediate() : 현재 스레드
2. Schedulers.single() : 재사용 가능한 하나의 스레드. 현재 수행 중인 리액터 플로우뿐만 아니라 호출되는 모든 작업이 동일한 하나의 스레드에서 실행된다.
3. Schedulers.newSingle() : 새로 생성한 전용 스레드
4. Schedulers.boundedElastic() : 작업량에 따라 스레드 숫자가 늘어나거나 줄어드는 신축성(elasticity 탄력성)있는 스레드풀
5. Schedulers.parallel() : 병렬 작업에 적합하도록 최적화된 고정 크기 워커(worker) 스레드풀
6. Schedulers.fromExecutorService() : ExecutorService 인스턴스를 감싸서 재사용

==> single(), newSingle(), parallel()은 논블로킹 작업에 사용되는 스레드를 생성하므로, block()같은 블로킹 코드가 사용되면 IllegalStateException 발생한다.

#### 리액터 플로우에서 스케줄러 변경하는 방법
- publishOn() :  
  호출되는 시점 이후로는 지정한 스케줄러를 사용. 사용하는 스케줄러를 여러 번 바꿀 수 있다.
- subscribeOn() :  
  플로우 전 단계에 걸쳐 사용되는 스케줄러를 지정. 플로우 전체에 영향을 미치므로 publishOn()에 비해 영향범위가 더 넓다.
