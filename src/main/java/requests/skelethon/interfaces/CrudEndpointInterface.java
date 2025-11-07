package requests.skelethon.interfaces;

import models.BaseModel;

public interface CrudEndpointInterface {
    Object post(BaseModel model);
    Object get();
    Object get(Integer id);
    Object update(BaseModel model);
    Object delete(Integer id);
}
