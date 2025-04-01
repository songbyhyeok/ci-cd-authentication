## 소개
![Image](https://github.com/user-attachments/assets/02e1be0a-19c9-44f4-9c22-52e76f718a8e)  

이 프로젝트는 GitHub Actions를 활용하여 CI/CD 파이프라인을 자동화하고, Spring Security와 JWT 토큰 인증 방식을 결합하여 안전한 인증 및 인가 시스템을 구축하는 것을 목표로 한다.

## 목표
- GitHub Actions를 활용한 CI/CD 파이프라인 구축
- Spring Security와 RESTful API를 활용한 JWT 인증 시스템 구현

## 기술 스택
### Back-end
<table border="1">
  <tr>
    <td>Java 17(Gradle)</td>
    <td>Spring(Boot, Security)</td>
    <td>JPA</td>
    <td>MySQL</td>
  </tr>
</table>

### DevOps
<table border="1">
  <tr>
    <td>AWS(EC2, RDS)</td>
    <td>GitHub Actions</td>
    <td>Postman</td>
    <td>Docker</td>
    <td>Ubuntu</td>
  </tr>
</table>

## 규칙
### branch rule
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

### commit rule
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
      <td><b>refactor</b></td>
      <td>코드 리팩토링</td>      
    </tr>
    <tr>
      <td><b>test</b></td>
      <td>테스트 코드</td>      
    </tr>
  </tbody>
</table>

### github issue rule
![image](https://github.com/user-attachments/assets/15aa6f57-a2d2-4f4b-b6f1-a2d9ef80f39c)  

**GitHub Flow** <br>
이 전략은 브랜치 구조와 규칙이 직관적이고 간단하여 소규모 개인 사이드 프로젝트에 적합하다. 또한, PR 방식의 자동화 시스템이 release 브랜치 역할을 대체할 수 있어, CI/CD를 활용한 자동화된 배포와 결합하면 더 유연하고 효율적인 개발이 가능하다.

## 개발일지
<ul>
  <li>
    <a href="https://songbyhyeok.github.io/ci-cd-auth-project/project-planning-and-design" target="_blank">1 - 프로젝트 기획 및 설계</a>
  </li>
  <li>
    <a href="https://songbyhyeok.github.io/ci-cd-auth-project/ci-cd-design/" target="_blank">2 - CI/CD 설계</a>
  </li>
  <li>
    <a href="https://songbyhyeok.github.io/ci-cd-auth-project/private-rds-issue/" target="_blank">3 - private RDS 접근하기</a>
  </li>
  <li>
    <a href="https://songbyhyeok.github.io/ci-cd-auth-project/overview-of-spring-security-structure/" target="_blank">4 - Spring Security 구조, 흐름</a>
  </li>
  <li>
    <a href="https://songbyhyeok.github.io/ci-cd-auth-project/introduction-to-jwt-in-spring-security/" target="_blank">5 - Spring Security에 JWT 도입</a>
  </li>
  <li>
    <a href="https://songbyhyeok.github.io/ci-cd-auth-project/secret-key-and-jwt-issuance-test/" target="_blank">6 - jwt 암호화와 secret key 생성 방법</a>
  </li>
</ul>



