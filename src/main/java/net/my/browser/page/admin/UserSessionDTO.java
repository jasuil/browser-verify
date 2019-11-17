package net.my.browser.page.admin;


import lombok.Data;

@Data
public class UserSessionDTO {

    String email;
    String ip;
    String name;
    UserDTO.Output.ValidState validState;

}
