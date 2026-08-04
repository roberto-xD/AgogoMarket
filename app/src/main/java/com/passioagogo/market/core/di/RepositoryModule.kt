package com.passioagogo.market.core.di

import com.passioagogo.market.data.catalog.CatalogRepositoryImpl
import com.passioagogo.market.data.catalog.ProductImageRepository
import com.passioagogo.market.data.catalog.ProductImageRepositoryImpl
import com.passioagogo.market.data.inventory.InventoryRepositoryImpl
import com.passioagogo.market.data.inventory.LocationRepositoryImpl
import com.passioagogo.market.domain.catalog.CatalogRepository
import com.passioagogo.market.domain.inventory.InventoryRepository
import com.passioagogo.market.domain.inventory.LocationRepository
import com.passioagogo.market.data.auth.SupabaseAuthRepository
import com.passioagogo.market.data.customers.CustomerRepository
import com.passioagogo.market.data.customers.CustomerRepositoryImpl
import com.passioagogo.market.data.promotions.PromotionRepositoryImpl
import com.passioagogo.market.data.purchases.PurchaseRepositoryImpl
import com.passioagogo.market.data.users.ProfilesAdminRepository
import com.passioagogo.market.data.users.ProfilesAdminRepositoryImpl
import com.passioagogo.market.domain.promotions.PromotionRepository
import com.passioagogo.market.data.purchases.SupplierRepositoryImpl
import com.passioagogo.market.data.sales.SalesRepositoryImpl
import com.passioagogo.market.domain.purchases.PurchaseRepository
import com.passioagogo.market.domain.purchases.SupplierRepository
import com.passioagogo.market.domain.auth.AuthRepository
import com.passioagogo.market.domain.sales.SalesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCatalogRepository(impl: CatalogRepositoryImpl): CatalogRepository

    @Binds
    @Singleton
    abstract fun bindProductImageRepository(
        impl: ProductImageRepositoryImpl,
    ): ProductImageRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    @Singleton
    abstract fun bindInventoryRepository(impl: InventoryRepositoryImpl): InventoryRepository

    @Binds
    @Singleton
    abstract fun bindSalesRepository(impl: SalesRepositoryImpl): SalesRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: SupabaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSupplierRepository(impl: SupplierRepositoryImpl): SupplierRepository

    @Binds
    @Singleton
    abstract fun bindPurchaseRepository(impl: PurchaseRepositoryImpl): PurchaseRepository

    @Binds
    @Singleton
    abstract fun bindPromotionRepository(impl: PromotionRepositoryImpl): PromotionRepository

    @Binds
    @Singleton
    abstract fun bindProfilesAdminRepository(
        impl: ProfilesAdminRepositoryImpl,
    ): ProfilesAdminRepository

    @Binds
    @Singleton
    abstract fun bindCustomerRepository(impl: CustomerRepositoryImpl): CustomerRepository
}
