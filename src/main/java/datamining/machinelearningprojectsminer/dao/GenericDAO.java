package datamining.machinelearningprojectsminer.dao;

import java.io.Serializable;
import java.util.List;

public interface GenericDAO<T, Type extends Serializable> {
	
	void beginTransaction();
	
	void commitTransaction();

	void closeSession();
	
	void save(T entity);
	
	void save(List<T> entities);
	
	void delete (T entity);
	
	T get(Type pk);
	
	List<T> listAll();

}