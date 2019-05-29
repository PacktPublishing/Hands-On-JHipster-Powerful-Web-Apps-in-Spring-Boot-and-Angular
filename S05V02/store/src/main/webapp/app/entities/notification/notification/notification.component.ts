import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { INotification } from 'app/shared/model/notification/notification.model';
import { AccountService } from 'app/core';
import { NotificationService } from './notification.service';

@Component({
  selector: 'jhi-notification',
  templateUrl: './notification.component.html'
})
export class NotificationComponent implements OnInit, OnDestroy {
  notifications: INotification[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected notificationService: NotificationService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.notificationService
      .query()
      .pipe(
        filter((res: HttpResponse<INotification[]>) => res.ok),
        map((res: HttpResponse<INotification[]>) => res.body)
      )
      .subscribe(
        (res: INotification[]) => {
          this.notifications = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInNotifications();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: INotification) {
    return item.id;
  }

  registerChangeInNotifications() {
    this.eventSubscriber = this.eventManager.subscribe('notificationListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
