package tr.org.lider.services;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tr.org.lider.constant.RoleConstants;
import tr.org.lider.constant.RoleTypeConstants;
import tr.org.lider.entities.RoleImpl;
import tr.org.lider.repositories.RoleRepository;
import tr.org.lider.repositories.RoleTypeRepository;
import tr.org.lider.entities.RoleTypeImpl;

@Service
public class RoleService {

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	RoleTypeRepository roleTypeRepository;

	@PostConstruct
	public void init() throws Exception {
		//roleTypeRepository.deleteAll();
		roleRepository.deleteAll();
		List<RoleTypeImpl> roleTypeList = new ArrayList<>();
		List<RoleTypeImpl> roleTypeList2 = new ArrayList<>();
		roleTypeList.add(new RoleTypeImpl("Admin Yetkisi", RoleTypeConstants.ADMIN));
		roleTypeList.add(new RoleTypeImpl("Arayüz Erişim Ayarları", RoleTypeConstants.CONSOLE));
		roleTypeList.add(new RoleTypeImpl("İstemci Yönetimi", RoleTypeConstants.COMPUTER));
		roleTypeList.add(new RoleTypeImpl("Kullanıcı Yönetimi", RoleTypeConstants.USER));
		roleTypeList.add(new RoleTypeImpl("Aktif Dizin Yönetimi", RoleTypeConstants.AD));
		roleTypeList.add(new RoleTypeImpl("Raporlama", RoleTypeConstants.REPORT));
		roleTypeList.add(new RoleTypeImpl("Kullanıcı Politika Yönetimi", RoleTypeConstants.POLICY));
		roleTypeList.add(new RoleTypeImpl("Ayarlar", RoleTypeConstants.SETTINGS));
		roleTypeList.add(new RoleTypeImpl("Tanımlamalar", RoleTypeConstants.DEFINITION));
		roleTypeList.add(new RoleTypeImpl("Eklentiler", RoleTypeConstants.PLUGIN));

		for (int i = 0; i < roleTypeList.size(); i++) {
			if (roleTypeRepository.findByCode(roleTypeList.get(i).getCode()).isEmpty()) {
				roleTypeList2.add(roleTypeList.get(i));
			}
		}
		roleTypeRepository.saveAll(roleTypeList2);

//		roleRepository.deleteAll();
		List<RoleImpl> roleList = new ArrayList<>();
		roleList.add(new RoleImpl("Tüm Yetkiler(Admin)", RoleConstants.ROLE_ADMIN, 0, findRoleTypeByCode(RoleTypeConstants.ADMIN)));
		roleList.add(new RoleImpl("Arayüz Erişim Yetkisi", RoleConstants.ROLE_USER, 1, findRoleTypeByCode(RoleTypeConstants.CONSOLE)));
		roleList.add(new RoleImpl("İstemci Yönetimi", RoleConstants.ROLE_COMPUTERS, 10, findRoleTypeByCode(RoleTypeConstants.COMPUTER)));
		roleList.add(new RoleImpl("İstemci Grup Yönetimi", RoleConstants.ROLE_COMPUTER_GROUPS, 12, findRoleTypeByCode(RoleTypeConstants.COMPUTER)));
		roleList.add(new RoleImpl("Kullanıcı İşlemleri", RoleConstants.ROLE_USERS, 20, findRoleTypeByCode(RoleTypeConstants.USER)));
		roleList.add(new RoleImpl("Kullanıcı Grup Yönetimi", RoleConstants.ROLE_USER_GROUPS, 22, findRoleTypeByCode(RoleTypeConstants.USER)));
		roleList.add(new RoleImpl("Kullanıcı Yetkilendirme(Sudo)", RoleConstants.ROLE_SUDO_GROUPS, 24, findRoleTypeByCode(RoleTypeConstants.USER)));
		roleList.add(new RoleImpl("AD Senkronizasyon", RoleConstants.ROLE_AD_SYNC, 30, findRoleTypeByCode(RoleTypeConstants.AD)));
		roleList.add(new RoleImpl("Detaylı İstemci Raporu", RoleConstants.ROLE_AGENT_INFO, 40, findRoleTypeByCode(RoleTypeConstants.REPORT)));
		roleList.add(new RoleImpl("Çalıştırılan Görevler Raporu", RoleConstants.ROLE_EXECUTED_TASK, 42, findRoleTypeByCode(RoleTypeConstants.REPORT)));
		roleList.add(new RoleImpl("Sistem Güncesi Raporu", RoleConstants.ROLE_OPERATION_LOG, 44, findRoleTypeByCode(RoleTypeConstants.REPORT)));
		roleList.add(new RoleImpl("Zamanlanmış Görev Raporu", RoleConstants.ROLE_SCHEDULE_TASK, 46, findRoleTypeByCode(RoleTypeConstants.REPORT)));
		roleList.add(new RoleImpl("Kullanıcı Oturum Raporu", RoleConstants.ROLE_USER_SESSION_REPORT, 48, findRoleTypeByCode(RoleTypeConstants.REPORT)));

		roleList.add(new RoleImpl("Politika Yönetimi", RoleConstants.ROLE_POLICY, 50, findRoleTypeByCode(RoleTypeConstants.POLICY)));
		roleList.add(new RoleImpl("Arayüz Erişim Ayarları", RoleConstants.ROLE_CONSOLE_ACCESS_SETTINGS, 60, findRoleTypeByCode(RoleTypeConstants.SETTINGS)));
		roleList.add(new RoleImpl("Sunucu Ayarları", RoleConstants.ROLE_SERVER_SETTINGS, 62, findRoleTypeByCode(RoleTypeConstants.SETTINGS)));
		roleList.add(new RoleImpl("Sunucu Bilgileri", RoleConstants.ROLE_SERVER_INFORMATION, 64, findRoleTypeByCode(RoleTypeConstants.SETTINGS)));
		roleList.add(new RoleImpl("Sistem Gözlemcisi Tanımları", RoleConstants.ROLE_CONKY_DEFINITION, 80, findRoleTypeByCode(RoleTypeConstants.DEFINITION)));
		roleList.add(new RoleImpl("Betik Tanımları", RoleConstants.ROLE_SCRIPT_DEFINITION, 82, findRoleTypeByCode(RoleTypeConstants.DEFINITION)));
		roleList.add(new RoleImpl("ETA Mesaj Tanımları", RoleConstants.ROLE_ETA_MESSAGE_DEFINITION, 84, findRoleTypeByCode(RoleTypeConstants.DEFINITION)));
		roleList.add(new RoleImpl("Kayıt Şablonları", RoleConstants.ROLE_REGISTRATION_TEMPLATE, 86, findRoleTypeByCode(RoleTypeConstants.DEFINITION)));

		// role plugins
		roleList.add(new RoleImpl("Kaynak Kullanımı", RoleConstants.ROLE_RESOURCE_USAGE, 100, findRoleTypeByCode(RoleTypeConstants.PLUGIN)));
		roleList.add(new RoleImpl("Oturum Yönetimi", RoleConstants.ROLE_SESSION_POWER, 101, findRoleTypeByCode(RoleTypeConstants.PLUGIN)));
		roleList.add(new RoleImpl("Root Parola Yönetimi", RoleConstants.ROLE_MANAGE_ROOT, 102, findRoleTypeByCode(RoleTypeConstants.PLUGIN)));
		roleList.add(new RoleImpl("Dosya Transferi", RoleConstants.ROLE_FILE_TRANSFER, 103, findRoleTypeByCode(RoleTypeConstants.PLUGIN)));
		roleList.add(new RoleImpl("Yerel Kullanıcı Yönetimi", RoleConstants.ROLE_LOCAL_USER, 104, findRoleTypeByCode(RoleTypeConstants.PLUGIN)));
		roleList.add(new RoleImpl("İstemci Oturum Açma Ayarları", RoleConstants.ROLE_LOGIN_MANAGER, 105, findRoleTypeByCode(RoleTypeConstants.PLUGIN)));
		roleList.add(new RoleImpl("Dosya Yönetimi", RoleConstants.ROLE_FILE_MANAGEMENT, 106, findRoleTypeByCode(RoleTypeConstants.PLUGIN)));
		roleList.add(new RoleImpl("Anlık Mesaj Gönder", RoleConstants.ROLE_SEND_MESSAGE, 107, findRoleTypeByCode(RoleTypeConstants.PLUGIN)));
		roleList.add(new RoleImpl("Sistem Gözlemcisi", RoleConstants.ROLE_CONKY, 108, findRoleTypeByCode(RoleTypeConstants.PLUGIN)));
		roleList.add(new RoleImpl("Paket Kur veya Kaldır", RoleConstants.ROLE_PACKAGE_INSTALL_REMOVE, 109, findRoleTypeByCode(RoleTypeConstants.PLUGIN)));
		roleList.add(new RoleImpl("Paket Kontrolü", RoleConstants.ROLE_PACKAGE_CONTROL, 110, findRoleTypeByCode(RoleTypeConstants.PLUGIN)));
		roleList.add(new RoleImpl("Paket Listesi", RoleConstants.ROLE_PACKAGE_LIST, 111, findRoleTypeByCode(RoleTypeConstants.PLUGIN)));
		roleList.add(new RoleImpl("Depo Yönetimi", RoleConstants.ROLE_PACKAGE_REPO, 112, findRoleTypeByCode(RoleTypeConstants.PLUGIN)));
		roleList.add(new RoleImpl("Servis Yönetimi", RoleConstants.ROLE_SERVICE_MANAGEMENT, 113, findRoleTypeByCode(RoleTypeConstants.PLUGIN)));
		roleList.add(new RoleImpl("Betik", RoleConstants.ROLE_SCRIPT, 114, findRoleTypeByCode(RoleTypeConstants.PLUGIN)));
		roleList.add(new RoleImpl("USB Kural Yönetimi(beyaz/kara liste)", RoleConstants.ROLE_USB_RULE, 115, findRoleTypeByCode(RoleTypeConstants.PLUGIN)));
		roleList.add(new RoleImpl("Aygıt Yönetimi", RoleConstants.ROLE_DEVICE_MANAGEMENT, 116, findRoleTypeByCode(RoleTypeConstants.PLUGIN)));
		roleList.add(new RoleImpl("Ağ Yönetimi", RoleConstants.ROLE_NETWORK_MANAGER, 117, findRoleTypeByCode(RoleTypeConstants.PLUGIN)));
		roleList.add(new RoleImpl("Uzak Erişim", RoleConstants.ROLE_REMOTE_ACCESS, 118, findRoleTypeByCode(RoleTypeConstants.PLUGIN)));
		roleList.add(new RoleImpl("Görev Geçmişi", RoleConstants.ROLE_TASK_HISTORY, 119, findRoleTypeByCode(RoleTypeConstants.PLUGIN)));
        roleList.add(new RoleImpl("İstemci Ayarları" , RoleConstants.ROLE_CLIENT_MANAGEMENT, 120, findRoleTypeByCode(RoleTypeConstants.PLUGIN)));

        List<RoleImpl> roleList2 = new ArrayList<>();
		for (int i = 0; i < roleList.size(); i++) {
			if (findRoleByValue(roleList.get(i).getValue()).isEmpty()) {
				roleList2.add(roleList.get(i));
			}
		}
		roleRepository.saveAll(roleList2);
	}

	public RoleImpl saveRole(RoleImpl role) {
		return roleRepository.save(role);
	}

	public List<RoleImpl> getRoles() {
		return roleRepository.findAllByOrderByOrderNumberAsc();
	}

	public RoleTypeImpl findRoleTypeByCode(String code) {
		List<RoleTypeImpl> roleType =  roleTypeRepository.findByCode(code);
		return roleType.get(0);
	}

	public List<RoleImpl> findRoleByValue(String value) {
		return roleRepository.findByValue(value);
	}

}
