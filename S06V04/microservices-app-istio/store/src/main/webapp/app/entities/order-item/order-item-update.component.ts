import { Component, OnInit } from '@angular/core';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { JhiAlertService } from 'ng-jhipster';
import { IOrderItem, OrderItem } from 'app/shared/model/order-item.model';
import { OrderItemService } from './order-item.service';
import { IProduct } from 'app/shared/model/product.model';
import { ProductService } from 'app/entities/product';
import { IProductOrder } from 'app/shared/model/product-order.model';
import { ProductOrderService } from 'app/entities/product-order';

@Component({
  selector: 'jhi-order-item-update',
  templateUrl: './order-item-update.component.html'
})
export class OrderItemUpdateComponent implements OnInit {
  orderItem: IOrderItem;
  isSaving: boolean;

  products: IProduct[];

  productorders: IProductOrder[];

  editForm = this.fb.group({
    id: [],
    quantity: [null, [Validators.required, Validators.min(0)]],
    totalPrice: [null, [Validators.required, Validators.min(0)]],
    status: [null, [Validators.required]],
    product: [null, Validators.required],
    order: [null, Validators.required]
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected orderItemService: OrderItemService,
    protected productService: ProductService,
    protected productOrderService: ProductOrderService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ orderItem }) => {
      this.updateForm(orderItem);
      this.orderItem = orderItem;
    });
    this.productService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IProduct[]>) => mayBeOk.ok),
        map((response: HttpResponse<IProduct[]>) => response.body)
      )
      .subscribe((res: IProduct[]) => (this.products = res), (res: HttpErrorResponse) => this.onError(res.message));
    this.productOrderService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IProductOrder[]>) => mayBeOk.ok),
        map((response: HttpResponse<IProductOrder[]>) => response.body)
      )
      .subscribe((res: IProductOrder[]) => (this.productorders = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(orderItem: IOrderItem) {
    this.editForm.patchValue({
      id: orderItem.id,
      quantity: orderItem.quantity,
      totalPrice: orderItem.totalPrice,
      status: orderItem.status,
      product: orderItem.product,
      order: orderItem.order
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const orderItem = this.createFromForm();
    if (orderItem.id !== undefined) {
      this.subscribeToSaveResponse(this.orderItemService.update(orderItem));
    } else {
      this.subscribeToSaveResponse(this.orderItemService.create(orderItem));
    }
  }

  private createFromForm(): IOrderItem {
    const entity = {
      ...new OrderItem(),
      id: this.editForm.get(['id']).value,
      quantity: this.editForm.get(['quantity']).value,
      totalPrice: this.editForm.get(['totalPrice']).value,
      status: this.editForm.get(['status']).value,
      product: this.editForm.get(['product']).value,
      order: this.editForm.get(['order']).value
    };
    return entity;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IOrderItem>>) {
    result.subscribe((res: HttpResponse<IOrderItem>) => this.onSaveSuccess(), (res: HttpErrorResponse) => this.onSaveError());
  }

  protected onSaveSuccess() {
    this.isSaving = false;
    this.previousState();
  }

  protected onSaveError() {
    this.isSaving = false;
  }
  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }

  trackProductById(index: number, item: IProduct) {
    return item.id;
  }

  trackProductOrderById(index: number, item: IProductOrder) {
    return item.id;
  }
}
