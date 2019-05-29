import { IProduct } from 'app/shared/model/product.model';
import { IProductOrder } from 'app/shared/model/product-order.model';

export const enum OrderItemStatus {
  AVAILABLE = 'AVAILABLE',
  OUT_OF_STOCK = 'OUT_OF_STOCK',
  BACK_ORDER = 'BACK_ORDER'
}

export interface IOrderItem {
  id?: number;
  quantity?: number;
  totalPrice?: number;
  status?: OrderItemStatus;
  product?: IProduct;
  order?: IProductOrder;
}

export class OrderItem implements IOrderItem {
  constructor(
    public id?: number,
    public quantity?: number,
    public totalPrice?: number,
    public status?: OrderItemStatus,
    public product?: IProduct,
    public order?: IProductOrder
  ) {}
}
