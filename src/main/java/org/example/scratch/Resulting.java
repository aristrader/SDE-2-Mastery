package org.example.scratch;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class Resulting {

    public static final Resulting obj = new Resulting();

    private String a;
    private String b;
}
