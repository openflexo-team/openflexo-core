# Appendix C. A complete example

This appendix reproduces one model and the script that runs it, both taken unchanged from the test resources of the
platform (`flexo-test-resources`: `FML/Library.fml` and `AutomatedTests/TestLibrary.fmlscript`). The platform's
automated tests load the model and run every `assert` of the script, so everything below is known to parse and to
behave as asserted. The tables after the listings point at the place of each construct in this specification.

## C.1 The model

```fml
use org.openflexo.foundation.fml.rt.FMLRTModelSlot as FMLRT;

import java.util.List;
import ["http://openflexo.org/test/TestResourceCenter/Library.fml/Catalog.fml"];

@URI("http://openflexo.org/test/TestResourceCenter/Library.fml")
@Version("1.0")
@Description("A library holding shelves of books")
public model Library {

	Catalog catalog;
	String lastCreatedShelf;

	public Catalog createCatalog() {
		catalog = new Catalog() with (name="catalog");
		return catalog;
	}

	public Shelf newShelf(String label) {
		Shelf shelf = new Shelf(parameters.label, 20);
		fire new ShelfCreated(parameters.label);
		return shelf;
	}

	listen ShelfCreated from this {
		lastCreatedShelf = evt.label;
	}

	event ShelfCreated {
		String label;
		create(String label) {
			label = parameters.label;
		}
	}

	public Shelf newSmallShelf(String label) {
		return new Shelf::withDefaultCapacity(parameters.label);
	}

	public List<Shelf> allShelves() {
		return select Shelf from this;
	}

	public int totalCapacity() {
		List<Shelf> shelves = select Shelf from this;
		int total = 0;
		for (int i = 0; i < shelves.size; i++) {
			total = total + shelves.get(i).capacity;
		}
		return total;
	}

	public int synchronizeIndex() {
		MatchingSet<IndexEntry> entries = begin match IndexEntry from this;
		for (Shelf shelf : select Shelf from this) {
			for (Book b : shelf.books) {
				match IndexEntry in entries from this where (book=b) unmatched: new IndexEntry(b);
			}
		}
		end match IndexEntry in entries unmatched: delete();
		List<IndexEntry> all = select IndexEntry from this;
		return all.size;
	}

	public concept IndexEntry {

		Book book;

		create(Book book) {
			book = parameters.book;
		}

		delete() {
		}
	}

	public abstract concept Publication {

		abstract String code;
	}

	public concept Shelf {

		String label;
		int capacity;
		Book[0,*] books;
		int bookCount values books.size;
		private String notes;

		String displayName {
			String get() {
				return "Shelf " + label;
			}
			set(String value) {
				label = value;
			}
		};

		create(required String label, int capacity=10) {
			label = parameters.label;
			capacity = parameters.capacity;
		}

		create::withDefaultCapacity(String label) {
			label = parameters.label;
			capacity = 10;
		}

		delete() {
		}

		@UI(
			@TextField(value="title", label="book_title")
		)
		public Book newBook(required String title) {
			Book book = new Book(parameters.title);
			books.add(book);
			return book;
		}

		public Novel newNovel(String title, String author) {
			Novel novel = new Novel(parameters.title, parameters.author);
			books.add(novel);
			return novel;
		}

		public rename(String newName) {
			displayName = parameters.newName;
		}

		public int countNovels() {
			int count = 0;
			for (Book book : books) {
				if (book instanceof Novel) {
					count = count + 1;
				}
			}
			return count;
		}

		public Book findBook(String title) {
			for (Book book : books) {
				if (book.title == parameters.title) {
					return book;
				}
			}
			return null;
		}

		public Book findBookByTitle(String title) {
			return select unique Book from (this) where (selected.title == parameters.title);
		}

		public int freeSlots() {
			int free = 0;
			int filled = bookCount;
			while (filled < capacity) {
				filled = filled + 1;
				free = free + 1;
			}
			return free;
		}

		public removeBook(Book book) {
			books.remove(parameters.book);
			delete parameters.book;
		}

		public concept Book extends Publication {

			String title;
			String code values "B-" + title;

			create(String title) {
				title = parameters.title;
			}

			delete() {
			}
		}

		public concept Novel extends Book {

			String author;

			create(String title, String author) {
				super(parameters.title);
				author = parameters.author;
			}

			delete() {
			}
		}
	}
}
```

## C.2 Where each construct is specified

| In the model | Construct | Section |
|---|---|---|
| `use … as FMLRT;` | technology-adapter alias | §3.4 |
| `import java.util.List;` | Java import | §3.5.1 |
| `import ["…/Catalog.fml"];` | import of a contained model by address | §3.5.2, §5.1.4 |
| `@URI`, `@Version`, `@Description` | annotations on a model | §11.3 |
| `public model Library` | model declaration | §5.1 |
| `Catalog catalog;` | property typed with a contained model | §6.1 |
| `new Catalog() with (name="catalog")` | instantiating a model | §9.7 |
| `event ShelfCreated`, `fire`, `listen … from this`, `evt` | events | §5.5, §10.6, §7.7 |
| `new Shelf::withDefaultCapacity(…)` | named creation scheme | §7.3.1 |
| `select Shelf from this` | query | §10.2 |
| `for (int i = 0; i < shelves.size; i++)` | classic `for`, property access `size` | §8.5, §9.6 |
| `begin match … end match` | matching set | §10.9 |
| `public abstract concept Publication`, `abstract String code;` | abstract concept and property | §5.2.2, §6.6 |
| `Book[0,*] books;` | multi-valued property | §4.6, §6.3 |
| `int bookCount values books.size;` | expression property | §6.4 |
| `String displayName { … };` | get/set property | §6.7 |
| `private String notes;` | visibility | §5.8 |
| `create(required String label, int capacity=10)` | creation scheme, required argument, default | §7.2, §7.3.1 |
| `create::withDefaultCapacity` | named creation scheme | §7.3.1 |
| `delete() { }` | deletion scheme | §7.3.2 |
| `@UI(@TextField(…))` | annotation on a behaviour | §11.5 |
| `books.add(book)`, `books.remove(…)` | multi-valued property | §6.3 |
| `select unique Book from (this) where (selected.title == …)` | single-result query | §10.2 |
| `while (filled < capacity)` | loop | §8.5 |
| `book instanceof Novel` | type test | §9.9 |
| `delete parameters.book;` | deletion | §10.4 |
| `public concept Book extends Publication` | nesting and inheritance | §5.2.1, §5.3 |
| `super(parameters.title)` | parent creation scheme | §5.3.2 |
| `parameters.title` | argument access | §7.2.1 |

## C.3 The script

```fml-script
virtualModel = load -r ["http://openflexo.org/test/TestResourceCenter/Library.fml"];
assert virtualModel != null;
assert virtualModel.name == "Library";

// Nested concepts: Book and Novel live in Shelf, which is a root concept
shelf = virtualModel.getFlexoConcept("Shelf");
assert shelf != null;
book = virtualModel.getFlexoConcept("Book");
assert book != null;
assert shelf.isRoot();
assert book.isRoot() == false;
assert book.containerFlexoConcept == shelf;
assert book.getURI() == "http://openflexo.org/test/TestResourceCenter/Library.fml#Shelf#Book";

// Concept inheritance: Novel extends Book
novel = virtualModel.getFlexoConcept("Novel");
assert novel != null;
assert book.isSuperConceptOf(novel);

// Contained VirtualModel: Catalog is stored in Library.fml/, its URI is computed from Library's one
assert virtualModel.getVirtualModels(true).size == 1;
assert virtualModel.getVirtualModels(true).get(0).getURI() == "http://openflexo.org/test/TestResourceCenter/Library.fml/Catalog.fml";
catalog = load -r ["http://openflexo.org/test/TestResourceCenter/Library.fml/Catalog.fml"];
assert catalog != null;
assert catalog.containerVirtualModel == virtualModel;

// Run-time
service ResourceCenterService add_temp_rc;
library = new Library() with (name="library");
assert library != null;

// Inside a behaviour of Library, "new Catalog() with (name=...)" creates a VirtualModelInstance of the
// contained VirtualModel, itself contained in the Library instance
libraryCatalog = library.createCatalog();
assert libraryCatalog != null;
assert libraryCatalog.name == "catalog";
assert libraryCatalog.containerVirtualModelInstance == library;
assert library.catalog == libraryCatalog;

// Creation scheme with a required parameter and a parameter declaring a default value
shelfCreation = shelf.getCreationSchemes().get(0);
assert shelfCreation.getSignature() == "create(label,capacity)";
assert shelfCreation.getParameter("label").getIsRequired();
assert shelfCreation.getParameter("capacity").getIsRequired() == false;
fiction = library.newShelf("Fiction");
assert fiction != null;
assert fiction.label == "Fiction";
// newShelf fires a ShelfCreated event, received by the listener declared in Library
assert library.lastCreatedShelf == "Fiction";
assert fiction.capacity == 20;
assert library.allShelves().size == 1;

// Inside a behaviour of Shelf, "new Book(...)" creates a Book contained in that Shelf instance
emma = fiction.newBook("Emma");
assert emma != null;
assert emma.title == "Emma";
assert emma.container == fiction;

dune = fiction.newNovel("Dune", "Frank Herbert");
assert dune != null;
assert dune.title == "Dune";
assert dune.author == "Frank Herbert";
assert dune.container == fiction;

// Concept instance role with multiple cardinality, and expression property (read-only)
assert shelf.getDeclaredProperty("books").cardinality.toString() == "ZeroMany";
assert shelf.getDeclaredProperty("bookCount").isReadOnly();
assert shelf.getDeclaredProperty("label").getURI() == "http://openflexo.org/test/TestResourceCenter/Library.fml#Shelf.label";
assert fiction.books.size == 2;
assert fiction.bookCount == 2;

// Get/set property: the getter computes, the setter (called by rename) writes the label
assert fiction.displayName == "Shelf Fiction";
fiction.rename("Classics");
assert fiction.label == "Classics";
assert fiction.displayName == "Shelf Classics";

// Iteration and conditional
assert fiction.countNovels() == 1;

// Early return from an iteration
assert fiction.findBook("Dune") == dune;
assert fiction.findBook("Ulysses") == null;

// Fetch request with a where clause
assert fiction.findBookByTitle("Emma") == emma;
assert fiction.findBookByTitle("Ulysses") == null;

// While loop
assert fiction.freeSlots() == 18;

// Abstract property: Publication declares code, Book implements it with an expression property
publication = virtualModel.getFlexoConcept("Publication");
assert publication != null;
assert publication.isAbstract();
assert publication.isSuperConceptOf(book);
assert emma.code == "B-Emma";
assert dune.code == "B-Dune";

// Named creation scheme, reached with new Shelf::withDefaultCapacity(...)
small = library.newSmallShelf("Poetry");
assert small != null;
assert small.label == "Poetry";
assert small.capacity == 10;

// Classic for loop, over the shelves Fiction (20) and Poetry (10)
assert library.totalCapacity() == 30;

// Matching: one index entry per book; running the synchronization again creates no duplicate
assert library.synchronizeIndex() == 2;
assert library.synchronizeIndex() == 2;

// Deletion: the book is removed from the multiple role, then deleted
fiction.removeBook(emma);
assert fiction.books.size == 1;

// Matching: the entry of the deleted book is not matched any more, and is deleted by end match
assert library.synchronizeIndex() == 1;
assert fiction.findBook("Emma") == null;

// Visibility: no keyword means Default
assert shelf.getDeclaredProperty("notes").visibility.toString() == "Private";
assert shelf.getDeclaredProperty("label").visibility.toString() == "Default";

log "Test PASSED: Library instantiates, with nested and inherited concepts, properties, parameters and control structures";
```

## C.4 Reading the script

| Part of the script | What it shows | Section |
|---|---|---|
| `load -r […]` | loading returns the virtual model | §12.3 |
| `getFlexoConcept("Shelf")`, `isRoot()`, `containerFlexoConcept` | the model level can be inspected | §13.1 |
| `service ResourceCenterService add_temp_rc;` | a writable resource center | §12.5 |
| `new Library() with (name="library")` | instantiating a model | §12.5 |
| `fiction.books.size == 2` | a multi-valued property is a list | §6.3 |
| `emma.container == fiction` | containment at run time | §13.2 |
| `library.lastCreatedShelf == "Fiction"` | event delivered synchronously | §10.6 |
| `fiction.displayName == "Shelf Fiction"` | get/set property | §6.7 |
| `fiction.freeSlots() == 18` | a `while` loop in a behaviour | §8.5 |
| `library.synchronizeIndex() == 2` run twice | a re-runnable matching | §10.9, §13.11 |
| `shelf.getDeclaredProperty("notes").visibility.toString() == "Private"` | visibility is metadata | §5.8 |
