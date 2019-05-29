import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { IProductCategory } from 'app/shared/model/product-category.model';
import { AccountService } from 'app/core';
import { ProductCategoryService } from './product-category.service';

@Component({
  selector: 'jhi-product-category',
  templateUrl: './product-category.component.html'
})
export class ProductCategoryComponent implements OnInit, OnDestroy {
  productCategories: IProductCategory[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected productCategoryService: ProductCategoryService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.productCategoryService
      .query()
      .pipe(
        filter((res: HttpResponse<IProductCategory[]>) => res.ok),
        map((res: HttpResponse<IProductCategory[]>) => res.body)
      )
      .subscribe(
        (res: IProductCategory[]) => {
          this.productCategories = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInProductCategories();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: IProductCategory) {
    return item.id;
  }

  registerChangeInProductCategories() {
    this.eventSubscriber = this.eventManager.subscribe('productCategoryListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
