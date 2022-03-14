package restproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import restproject.entity.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.http.RequestEntity.put;

@Component
public class Communication {
    private final RestTemplate restTemplate;
    private HttpHeaders headers;
    private String URL = "http://94.198.50.185:7081/api/users";


    @Autowired
    public Communication(RestTemplate restTemplate, HttpHeaders headers) {
        this.restTemplate = restTemplate;
        this.headers = headers;
        this.headers.set("Cookie",
                String.join(";", restTemplate.headForHeaders(URL).get("Set-Cookie")));
    }

    public String getAnswer() {
        return addUser().getBody() + updateUser().getBody() + deleteUser().getBody();
    }

    // Получение всех пользователей -  …/api/users ( GET )
    public List<User> getAllUsers() {
        ResponseEntity<List<User>> responseEntity = restTemplate.exchange(URL, HttpMethod.GET, null, new ParameterizedTypeReference<List<User>>() {
        });
        System.out.println(responseEntity.getHeaders());
        HttpHeaders responseEntityHeaders = responseEntity.getHeaders();
        Set<String> keys = responseEntityHeaders.keySet();
        String cookie = "";
        for (String header : keys) {
            if (header.equals("Set-Cookie")) {
                cookie = responseEntityHeaders.get(header).get(0);
            }
        }
        String jsessionid = cookie.split(";")[0];

        headers.add("Set-Cookie", "JSESSIONID=" + jsessionid);
        return responseEntity.getBody();
    }


    // Добавление пользователя - …/api/users ( POST )
    public ResponseEntity<String> addUser() {
        User user = new User(3L, "James", "Brown", (byte) 5);
        HttpEntity<User> entity = new HttpEntity<>(user, headers);
        System.out.println(headers);
        return restTemplate.postForEntity(URL, entity, String.class);
    }

    // Изменение пользователя - …/api/users ( PUT )
    private ResponseEntity<String> updateUser() {
        User user = new User(3L, "Thomas", "Shelby", (byte) 5);
        HttpEntity<User> entity = new HttpEntity<>(user, headers);
        return restTemplate.exchange(URL, HttpMethod.PUT, entity, String.class, 3);
    }

    // Удаление пользователя - …/api/users /{id} ( DELETE )
    private ResponseEntity<String> deleteUser() {
        Map<String, Long> uriVariables = new HashMap<>() {{
            put("id", 3L);
        }};
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        return restTemplate.exchange(URL + "/{id}", HttpMethod.DELETE, entity, String.class, uriVariables);
    }
}
