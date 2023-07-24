package dev.sodev.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@Getter
@AllArgsConstructor
public enum SkillCode {
    JAVA("java", 1),
    KOTLIN("kotlin",2),
    NODE_JS("node.js",3),
    PYTHON("python",4),
    C("c",5),
    CPLUSPLUS("c++",6),
    CSHARP("c#",7),
    SPRING("spring",8),
    SPRING_BOOT("spring boot",9),
    JPA("jpa",10),
    QUERYDSL("queryDsl",11),
    JPQL("jpql",12),
    GOLANG("golang",13),
    PHP("php",14),
    HTML("html",15),
    CSS("css",16),
    THYMELEAF("thymeleaf",17),
    JQUERY("jquery",18),
    REACT("react",19),
    VUE_JS("vue.js",20),
    TYPESCRIPT("typescript",21),
    JAVASCRIPT("javascript",22),
    SQL("mysql",23),
    MYBATIS("mybatis",24),
    POSTGRESQL("postgresql",25),
    AWS("aws",26),
    JENKINS("jenkins",27),
    KUBERNETES("kubernetes",28),
    DOCKER("docker",29),
    LINUX("linux",30),
    REDIS("redis",31),
    KAFKA("kafka",32),
    ;
    private String skill;
    private Integer skillCode;

    private static final Map<String, Integer> SkillsMap = Collections.unmodifiableMap(Stream.of(values()).collect(Collectors.toMap(SkillCode::getSkill, SkillCode::getSkillCode)));
    public static Integer findSkillCode(String skill){
        return SkillsMap.get(skill);
    }

}
