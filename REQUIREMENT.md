# 요구사항

- 디비에 2개의 테이블이 있다. 각각 Devices, Tasks 테이블이 있다.
- Devices 테이블은 id, name, type, created_at, updated_at 컬럼을 가지고 있다.
- Tasks 테이블은 id, device_id, name, description, created_at, updated_at 컬럼을 가지고 있다.
- Tasks 테이블의 device_id는 Devices 테이블의 id와 매핑된다.
- Tasks 테이블의 name은 255자 이하의 문자열이다.
- Tasks 테이블의 description은 1000자 이하의 문자열이다.
- DeviceController, DeviceService, DeviceRepository 를 생성하고, Rest api에 맞게 구현한다.
- TaskController, TaskService, TaskRepository 를 생성하고, Rest api에 맞게 구현한다.
- DeviceController에는 기본적인 CRUD api를 구현한다.
- TaskController에는 기본적인 CRUD api를 구현한다.
- DeviceController에는 Device 목록과 Task 목록이 결합된 api를 구현한다. 이름은 getDevicesWithTasks 이다. 이것은 Paging이 가능한 api이다.
- TaskController에는 Task 목록이 결합된 api를 구현한다. 이름은 getTasksWithDevice 이다. 이것은 Paging이 가능한 api이다.
- 테스트 코드를 작성한다.
