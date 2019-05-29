export interface ICountry {
  id?: number;
  name?: string;
  capital?: string;
  population?: number;
}

export class Country implements ICountry {
  constructor(public id?: number, public name?: string, public capital?: string, public population?: number) {}
}
