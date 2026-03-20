package fun.cyhgraph.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.cyhgraph.entity.AddressBook;
import fun.cyhgraph.mapper.AddressBookMapper;
import fun.cyhgraph.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
    @Autowired
    private AddressBookMapper addressBookMapper;

    public List<AddressBook> list(AddressBook addressBook) {
        return addressBookMapper.selectList(null);
    }

    public boolean save(AddressBook addressBook) {
        return addressBookMapper.insert(addressBook) > 0;
    }

    public void addAddress(AddressBook addressBook) {
        addressBookMapper.insert(addressBook);
    }

    public void updateAddress(AddressBook addressBook) {
        addressBookMapper.updateById(addressBook);
    }

    public AddressBook getById(Integer id) {
        return addressBookMapper.selectById(id);
    }

    // 【核心修复】改为 Integer
    public void deleteById(Integer id) {
        addressBookMapper.deleteById(id);
    }

    public void setDefault(AddressBook addressBook) {
    }

    public AddressBook getDefault() {
        return new AddressBook();
    }
}
