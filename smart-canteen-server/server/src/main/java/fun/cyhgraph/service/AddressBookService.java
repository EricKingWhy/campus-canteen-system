package fun.cyhgraph.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.cyhgraph.entity.AddressBook;
import java.util.List;

public interface AddressBookService extends IService<AddressBook> {
    List<AddressBook> list(AddressBook addressBook);

    boolean save(AddressBook addressBook);

    void addAddress(AddressBook addressBook);

    void updateAddress(AddressBook addressBook);

    AddressBook getById(Integer id);

    // 【核心修复】改为 Integer
    void deleteById(Integer id);

    void setDefault(AddressBook addressBook);

    AddressBook getDefault();
}
