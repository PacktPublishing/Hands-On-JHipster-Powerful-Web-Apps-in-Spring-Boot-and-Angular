import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService, JhiDataUtils } from 'ng-jhipster';

import { IBlogEntry } from 'app/shared/model/blog-entry.model';
import { AccountService } from 'app/core';
import { BlogEntryService } from './blog-entry.service';

@Component({
  selector: 'jhi-blog-entry',
  templateUrl: './blog-entry.component.html'
})
export class BlogEntryComponent implements OnInit, OnDestroy {
  blogEntries: IBlogEntry[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected blogEntryService: BlogEntryService,
    protected jhiAlertService: JhiAlertService,
    protected dataUtils: JhiDataUtils,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.blogEntryService
      .query()
      .pipe(
        filter((res: HttpResponse<IBlogEntry[]>) => res.ok),
        map((res: HttpResponse<IBlogEntry[]>) => res.body)
      )
      .subscribe(
        (res: IBlogEntry[]) => {
          this.blogEntries = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInBlogEntries();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: IBlogEntry) {
    return item.id;
  }

  byteSize(field) {
    return this.dataUtils.byteSize(field);
  }

  openFile(contentType, field) {
    return this.dataUtils.openFile(contentType, field);
  }

  registerChangeInBlogEntries() {
    this.eventSubscriber = this.eventManager.subscribe('blogEntryListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
