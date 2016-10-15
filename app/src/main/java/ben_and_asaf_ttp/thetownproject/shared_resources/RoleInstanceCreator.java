package ben_and_asaf_ttp.thetownproject.shared_resources;

import java.lang.reflect.Type;

import com.google.gson.InstanceCreator;

public class RoleInstanceCreator implements InstanceCreator<Role>{
	public Role createInstance(Type type){
		if(type instanceof Citizen){
			return new Citizen();
		}else if(type instanceof Killer){
			return new Killer();
		}else if(type instanceof Healer){
			return new Healer();
		}else if (type instanceof Snitch){
			return new Snitch();
		}else{
			return null;
		}
	}
}