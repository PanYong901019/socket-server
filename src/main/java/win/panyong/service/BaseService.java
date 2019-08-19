package win.panyong.service;


import win.panyong.utils.AppCache;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseService {
    @Autowired
    protected AppCache appCache;

}
