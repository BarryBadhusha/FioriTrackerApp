package com.novigo.fiori.trackerapp.viewmodel;

import android.app.Application;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import android.os.Parcelable;

import com.novigo.fiori.trackerapp.viewmodel.customer.CustomerViewModel;
import com.novigo.fiori.trackerapp.viewmodel.productcategory.ProductCategoryViewModel;
import com.novigo.fiori.trackerapp.viewmodel.producttext.ProductTextViewModel;
import com.novigo.fiori.trackerapp.viewmodel.product.ProductViewModel;
import com.novigo.fiori.trackerapp.viewmodel.purchaseorderheader.PurchaseOrderHeaderViewModel;
import com.novigo.fiori.trackerapp.viewmodel.purchaseorderitem.PurchaseOrderItemViewModel;
import com.novigo.fiori.trackerapp.viewmodel.salesorderheader.SalesOrderHeaderViewModel;
import com.novigo.fiori.trackerapp.viewmodel.salesorderitem.SalesOrderItemViewModel;
import com.novigo.fiori.trackerapp.viewmodel.stock.StockViewModel;
import com.novigo.fiori.trackerapp.viewmodel.supplier.SupplierViewModel;


/**
 * Custom factory class, which can create view models for entity subsets, which are
 * reached from a parent entity through a navigation property.
 */
public class EntityViewModelFactory implements ViewModelProvider.Factory {

	// application class
    private Application application;
	// name of the navigation property
    private String navigationPropertyName;
	// parent entity
    private Parcelable entityData;

	/**
	 * Creates a factory class for entity view models created following a navigation link.
	 *
	 * @param application parent application
	 * @param navigationPropertyName name of the navigation link
	 * @param entityData parent entity
	 */
    public EntityViewModelFactory(Application application, String navigationPropertyName, Parcelable entityData) {
        this.application = application;
        this.navigationPropertyName = navigationPropertyName;
        this.entityData = entityData;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        T retValue = null;
		switch(modelClass.getSimpleName()) {



			case "CustomerViewModel":
				retValue = (T) new CustomerViewModel(application, navigationPropertyName, entityData);
				break;
			case "ProductCategoryViewModel":
				retValue = (T) new ProductCategoryViewModel(application, navigationPropertyName, entityData);
				break;
			case "ProductTextViewModel":
				retValue = (T) new ProductTextViewModel(application, navigationPropertyName, entityData);
				break;
			case "ProductViewModel":
				retValue = (T) new ProductViewModel(application, navigationPropertyName, entityData);
				break;
			case "PurchaseOrderHeaderViewModel":
				retValue = (T) new PurchaseOrderHeaderViewModel(application, navigationPropertyName, entityData);
				break;
			case "PurchaseOrderItemViewModel":
				retValue = (T) new PurchaseOrderItemViewModel(application, navigationPropertyName, entityData);
				break;
			case "SalesOrderHeaderViewModel":
				retValue = (T) new SalesOrderHeaderViewModel(application, navigationPropertyName, entityData);
				break;
			case "SalesOrderItemViewModel":
				retValue = (T) new SalesOrderItemViewModel(application, navigationPropertyName, entityData);
				break;
			case "StockViewModel":
				retValue = (T) new StockViewModel(application, navigationPropertyName, entityData);
				break;
			case "SupplierViewModel":
				retValue = (T) new SupplierViewModel(application, navigationPropertyName, entityData);
				break;
		}
		return retValue;
	}
}