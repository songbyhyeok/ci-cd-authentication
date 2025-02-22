# 소개
이 프로젝트는 개인 사이드 프로젝트로, Spring MVC와 Thymeleaf를 사용하여 웹 애플리케이션을 개발하고자 합니다. Spring Security를 활용한 안전한 인증 및 인가 시스템을 구축하고, GitHub Actions를 통해 CI/CD 파이프라인을 자동화하여 효율적인 배포 환경을 마련할 계획입니다. 또한, JWT 토큰과 OAuth 2.0을 이용한 인증 방식을 적용하여 보안을 강화하고, 사용자가 회원가입과 로그인 기능을 통해 시스템에 안전하게 접근할 수 있도록 구현할 예정입니다.

## 프로젝트 목표
- GitHub Actions를 활용한 CI/CD 파이프라인 구축
- Spring MVC와 Thymeleaf를 활용한 웹 애플리케이션 개발
- 사용자 등록(회원가입) 및 로그인 기능 구현
- Spring Security를 통한 인증 및 인가 구현, JWT Token과 OAuth 2.0을 이용한 인증 방식 적용

## 기술 스택
Java 17(Gradle)
Spring(Boot, Security, Thymeleaf)
JPA
MySQL
Swagger
GitHub Actions
Ubuntu
Docker
AWS(EC2, RDS)

## 목업

## ERD

## 컨벤션
<details>
<summary>열기</summary>

## branch rule
<table border="1">
  <thead>
    <tr>
      <th>Type</b></th>
      <th>Description</th>      
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>main</td>
      <td>최종 배포 버전의 코드가 유지되는 브랜치</td>            
    </tr>
    <tr>
      <td>feature</td>
      <td>새로운 기능 개발을 위한 독립적인 브랜치로, 개발 후 main에 병합</td>            
    </tr>
  </tbody>
</table>

GitHub Flow 전략을 선택하게 되었다.<br><br>
이 전략을 선택한 이유는 브랜치 구조와 규칙이 직관적이고 간단하여 소규모 개인 사이드 프로젝트에 적합하다. 또한, PR 방식의 자동화 시스템이 release 브랜치 역할을 대체할 수 있어, CI/CD를 활용한 자동화된 배포와 결합하면 더 유연하고 효율적인 개발이 가능하다.

## commit rule
<table border="1">
  <thead>
    <tr>
      <th>Type</b></th>
      <th>Description</th>      
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><b>feature</b></td>
      <td>새로운 기능 추가</td>      
    </tr>
    <tr>
      <td><b>fix</b></td>
      <td>버그 수정</td>      
    </tr>
    <tr>
      <td><b>test</b></td>
      <td>테스트 코드</td>      
    </tr>
  </tbody>
</table>

### Example
<table border="1">
  <thead>
    <tr>
      <th>Type</b></th>
      <th>Issue Number</b></th>
      <th>Description</th>      
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>feat</td>
      <td>#01</td>      
      <td>기능 구현</td>      
    </tr>
  </tbody>
</table>

</details>
