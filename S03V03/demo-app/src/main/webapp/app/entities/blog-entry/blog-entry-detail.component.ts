import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { JhiDataUtils } from 'ng-jhipster';

import { IBlogEntry } from 'app/shared/model/blog-entry.model';

@Component({
  selector: 'jhi-blog-entry-detail',
  templateUrl: './blog-entry-detail.component.html'
})
export class BlogEntryDetailComponent implements OnInit {
  blogEntry: IBlogEntry;

  constructor(protected dataUtils: JhiDataUtils, protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ blogEntry }) => {
      this.blogEntry = blogEntry;
    });
  }

  byteSize(field) {
    return this.dataUtils.byteSize(field);
  }

  openFile(contentType, field) {
    return this.dataUtils.openFile(contentType, field);
  }
  previousState() {
    window.history.back();
  }
}
