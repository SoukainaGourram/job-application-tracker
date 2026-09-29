import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CompanyFormComponent } from './company-form.component';
import { CompanyService } from '../../../core/services/company.service';
import { of, throwError } from 'rxjs';
import { ActivatedRoute, Router, provideRouter } from '@angular/router';
import { Company } from '../../../core/models/company.models';

const MOCK_COMPANY: Company = {
  id: 10,
  name: 'TechCorp',
  industry: 'Tech',
  location: 'Paris',
  size: 'Grand Groupe',
  website: 'https://techcorp.com',
  description: 'Leader tech',
  notes: 'Notes privées',
  contacts: [],
  offersCount: 2,
  createdAt: '2026-09-29T10:00:00Z',
  updatedAt: '2026-09-29T10:00:00Z',
};

describe('CompanyFormComponent', () => {
  let component: CompanyFormComponent;
  let fixture: ComponentFixture<CompanyFormComponent>;
  let companyServiceMock: any;
  let router: Router;

  beforeEach(async () => {
    companyServiceMock = {
      getCompanyById: vi.fn().mockReturnValue(of(MOCK_COMPANY)),
      createCompany: vi.fn().mockReturnValue(of(MOCK_COMPANY)),
      updateCompany: vi.fn().mockReturnValue(of(MOCK_COMPANY)),
    };

    await TestBed.configureTestingModule({
      imports: [CompanyFormComponent],
      providers: [
        { provide: CompanyService, useValue: companyServiceMock },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => (key === 'id' ? null : null),
              },
            },
          },
        },
        provideRouter([
          { path: 'companies', component: class {} },
          { path: 'companies/:id', component: class {} },
        ]),
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate');

    fixture = TestBed.createComponent(CompanyFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create CompanyFormComponent in create mode', () => {
    expect(component).toBeTruthy();
    expect(component.isEditMode()).toBe(false);
  });

  it('should be invalid when name is empty', () => {
    component.companyForm.controls['name'].setValue('');
    expect(component.companyForm.valid).toBe(false);
    expect(component.companyForm.controls['name'].errors?.['required']).toBeTruthy();
  });

  it('should be valid with valid fields', () => {
    component.companyForm.patchValue({
      name: 'Acme Inc',
      website: 'https://acme.com',
      industry: 'Industry',
    });
    expect(component.companyForm.valid).toBe(true);
  });

  it('should call createCompany and navigate on submit in create mode', () => {
    component.companyForm.patchValue({
      name: 'TechCorp',
      industry: 'Tech',
      website: 'https://techcorp.com',
    });

    component.onSubmit();

    expect(companyServiceMock.createCompany).toHaveBeenCalledWith(
      expect.objectContaining({ name: 'TechCorp', industry: 'Tech' })
    );
    expect(router.navigate).toHaveBeenCalledWith(['/companies', 10]);
  });

  it('should handle creation error', () => {
    companyServiceMock.createCompany.mockReturnValue(throwError(() => new Error('Error')));
    component.companyForm.patchValue({ name: 'TechCorp' });

    component.onSubmit();

    expect(component.errorMessage()).toBe('Erreur lors de la création de l\'entreprise.');
    expect(component.submitting()).toBe(false);
  });

  it('should load company and update in edit mode', () => {
    component.isEditMode.set(true);
    component.companyId.set(10);
    component.loadCompany(10);

    expect(companyServiceMock.getCompanyById).toHaveBeenCalledWith(10);
    expect(component.companyForm.value.name).toBe('TechCorp');

    component.companyForm.patchValue({ name: 'TechCorp Updated' });
    component.onSubmit();

    expect(companyServiceMock.updateCompany).toHaveBeenCalledWith(
      10,
      expect.objectContaining({ name: 'TechCorp Updated' })
    );
    expect(router.navigate).toHaveBeenCalledWith(['/companies', 10]);
  });
});
