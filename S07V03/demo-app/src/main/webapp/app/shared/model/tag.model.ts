import { IBlogEntry } from 'app/shared/model/blog-entry.model';

export interface ITag {
  id?: number;
  name?: string;
  entries?: IBlogEntry[];
}

export class Tag implements ITag {
  constructor(public id?: number, public name?: string, public entries?: IBlogEntry[]) {}
}
