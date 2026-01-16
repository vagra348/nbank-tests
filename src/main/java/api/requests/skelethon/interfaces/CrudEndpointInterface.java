package api.requests.skelethon.interfaces;

import api.models.BaseModel;

public interface CrudEndpointInterface {
    Object post(BaseModel model);

    Object get();

    Object get(Integer id);

    Object get(String param, Integer id);

    Object update(BaseModel model);

    Object delete(Integer id);
}
