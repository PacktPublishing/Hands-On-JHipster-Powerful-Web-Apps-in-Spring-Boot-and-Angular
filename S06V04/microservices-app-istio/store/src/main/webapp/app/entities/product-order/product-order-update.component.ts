import { Component, OnInit } from '@angular/core';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';
import { JhiAlertService } from 'ng-jhipster';
import { IProductOrder, ProductOrder } from 'app/shared/model/product-order.model';
import { ProductOrderService } from './product-order.service';
import { ICustomer } from 'app/shared/model/customer.model';
import { CustomerService } from 'app/entities/customer';

@Component({
  selector: 'jhi-product-order-update',
  templateUrl: './product-order-update.component.html'
})
export class ProductOrderUpdateComponent implements OnInit {
  productOrder: IProductOrder;
  isSaving: boolean;

  customers: ICustomer[];

  editForm = this.fb.group({
    id: [],
    placedDate: [null, [Validators.required]],
    status: [null, [Validators.required]],
    code: [null, [Validators.required]],
    invoiceId: [],
    customer: [null, Validators.required]
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected productOrderService: ProductOrderService,
    protected customerService: CustomerService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ productOrder }) => {
      this.updateForm(productOrder);
      this.productOrder = productOrder;
    });
    this.customerService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<ICustomer[]>) => mayBeOk.ok),
        map((response: HttpResponse<ICustomer[]>) => response.body)
      )
      .subscribe((res: ICustomer[]) => (this.customers = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(productOrder: IProductOrder) {
    this.editForm.patchValue({
      id: productOrder.id,
      placedDate: productOrder.placedDate != null ? productOrder.placedDate.format(DATE_TIME_FORMAT) : null,
      status: productOrder.status,
      code: productOrder.code,
      invoiceId: productOrder.invoiceId,
      customer: productOrder.customer
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const productOrder = this.createFromForm();
    if (productOrder.id !== undefined) {
      this.subscribeToSaveResponse(this.productOrderService.update(productOrder));
    } else {
      this.subscribeToSaveResponse(this.productOrderService.create(productOrder));
    }
  }

  private createFromForm(): IProductOrder {
    const entity = {
      ...new ProductOrder(),
      id: this.editForm.get(['id']).value,
      placedDate:
        this.editForm.get(['placedDate']).value != null ? moment(this.editForm.get(['placedDate']).value, DATE_TIME_FORMAT) : undefined,
      status: this.editForm.get(['status']).value,
      code: this.editForm.get(['code']).value,
      invoiceId: this.editForm.get(['invoiceId']).value,
      customer: this.editForm.get(['customer']).value
    };
    return entity;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IProductOrder>>) {
    result.subscribe((res: HttpResponse<IProductOrder>) => this.onSaveSuccess(), (res: HttpErrorResponse) => this.onSaveError());
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

  trackCustomerById(index: number, item: ICustomer) {
    return item.id;
  }
}
