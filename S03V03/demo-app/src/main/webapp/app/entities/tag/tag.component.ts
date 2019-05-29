import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { ITag } from 'app/shared/model/tag.model';
import { AccountService } from 'app/core';
import { TagService } from './tag.service';

@Component({
  selector: 'jhi-tag',
  templateUrl: './tag.component.html'
})
export class TagComponent implements OnInit, OnDestroy {
  tags: ITag[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected tagService: TagService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.tagService
      .query()
      .pipe(
        filter((res: HttpResponse<ITag[]>) => res.ok),
        map((res: HttpResponse<ITag[]>) => res.body)
      )
      .subscribe(
        (res: ITag[]) => {
          this.tags = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInTags();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: ITag) {
    return item.id;
  }

  registerChangeInTags() {
    this.eventSubscriber = this.eventManager.subscribe('tagListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
