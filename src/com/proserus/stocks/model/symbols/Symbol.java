package com.proserus.stocks.model.symbols;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Transient;

import org.jfree.data.time.Year;

@Entity
@NamedQueries( { @NamedQuery(name = "symbol.findAll", query = "SELECT s FROM Symbol s"),
        @NamedQuery(name = "symbol.findAllByCurrency", query = "SELECT s FROM Symbol s WHERE currency = :currency"),
        @NamedQuery(name = "symbol.findByTicker", query = "SELECT s FROM Symbol s WHERE ticker = :ticker") })
public class Symbol implements Comparable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Integer id;

	public Integer getId() {
		return id;
	}

	private static final String EMPTY_STR = "";

	// http://quote.yahoo.com/d/quotes.csv?s=usdcad=X&f=nl1&e=.csv

	// doc: http://code.google.com/p/yahoo-finance-managed/


	// @CollectionOfElements(targetElement=java.lang.String.class)
	// @JoinTable(name="BOOK_CHAPTER",
	// joinColumns=@JoinColumn(name="BOOK_ID"))
	// @MapKey (columns=@Column(name="CHAPTER_KEY"))
	// @Column(name="CHAPTER")
	//	
	// @CollectionOfElements(targetElement=java.lang.Float.class)
	// @JoinTable(name="SYMBOL_PRICE",
	// joinColumns=@JoinColumn(name="SYMBOL_ID"))
	// @MapKey (name="PRICE_KEY")
	// @Column(name="PRICE")
	// @CollectionOfElements(targetElement = com.proserus.stocks.model.symbols.HistoricalPrice.class)
	@OneToMany(targetEntity=HistoricalPriceImpl.class, cascade = CascadeType.ALL)
	// TODO nullable=false
	@JoinTable(name = "SYMBOL_PRICES", joinColumns = { @JoinColumn(name = "symbolId") }, inverseJoinColumns = { @JoinColumn(name = "priceId") })
	private Collection<HistoricalPrice> prices = new LinkedList<HistoricalPrice>();

	@Transient
	private Map<Year, HistoricalPrice> mapPrices = new HashMap<Year, HistoricalPrice>();

	public Map<Year, HistoricalPrice> getMapPrices() {
		return mapPrices;
	}

	@PostLoad
	private void postLoad() {
		setPrices(prices);
	}

	@Column(nullable = false)
	private boolean isCustomPriceFirst = false;

	public boolean isCustomPriceFirst() {
		return isCustomPriceFirst;
	}

	public void setCustomPriceFirst(boolean isCustomPriceFirst) {
		this.isCustomPriceFirst = isCustomPriceFirst;
	}

	private String name = EMPTY_STR;

	@Column(unique = true, nullable = false)
	//@Check(constraints = "LTRIM(TICKER) != ''")
	//TODO add constraint to forbid empty string (or just blank space): LTRIM(LABEL) != ''
	private String ticker;
	
	private CurrencyEnum currency = CurrencyEnum.valueOf(Currency.getInstance(Locale.getDefault()).toString());

	private SectorEnum sector;
	
	
	public SectorEnum getSector() {
		if (null == this.sector) {
			return SectorEnum.UNKNOWN;
		}
		return sector;
	}

	public void setSector(SectorEnum sector) {
		this.sector = sector;
	}

	public Symbol() {
		// for JPA
	}

	public void setName(String name) {
		if (name == null) {
			throw new NullPointerException();
		}
		
		//Empty is allowed

		this.name = name;
	}

	public void setTicker(String ticker) {
		if (ticker == null) {
			throw new NullPointerException();
		}
		if (ticker.isEmpty()) {
			throw new IllegalArgumentException();
		}

		this.ticker = ticker.toLowerCase();
	}

	public void setPrice(BigDecimal price, Year year) {
		if (year == null || price==null) {
			throw new NullPointerException();
		}
		
		HistoricalPriceImpl histPrice = (HistoricalPriceImpl) mapPrices.get(year);
		if (histPrice == null) {
			histPrice = new HistoricalPriceImpl();
			histPrice.setYear(year);
			histPrice.setCustomPrice(price);
			mapPrices.put(year, histPrice);
			prices.add(histPrice);
		}
		//TODO Manage Date better
		if (histPrice.getYear() == null) {
		}
		histPrice.setPrice(price);
	}

	public void setCurrency(CurrencyEnum currency) {
		if (currency == null) {
			throw new NullPointerException();
		}
		this.currency = currency;
	}

	public String getName() {
		return name;
	}

	public String getTicker() {
		return ticker;
	}

	/*
	 * public Float getPrice() { return price; }
	 */
	public HistoricalPrice getPrice(Year year) {
		if (year == null) {
			throw new NullPointerException();
		}
		
		HistoricalPriceImpl h = (HistoricalPriceImpl)mapPrices.get(year);
		if(h == null){
			h = new HistoricalPriceImpl();
			h.setCustomPrice(BigDecimal.ZERO);
			h.setPrice(BigDecimal.ZERO);
			h.setYear(year);
		}
		return h;
	}
	
	public Collection<HistoricalPrice> getHistoricalPricesValues() {
		return prices;
	}

	public Collection<HistoricalPrice> getPrices() {
		return prices;
	}

	public void addPrice(HistoricalPrice price){
		if(price == null){
			throw new NullPointerException();
		}
		
		//TODO Do I need a map of prices ?
		if(mapPrices.containsKey(price.getYear())){
			prices.remove(price);
		}
		prices.add(price);
		mapPrices.put(price.getYear(), price);
	}
	public void setPrices(Collection<HistoricalPrice> prices) {
		if (prices == null || prices.contains(null)) {
			throw new NullPointerException();
		}

		if (prices.contains("")) {
			throw new IllegalArgumentException();
		}

		this.prices = prices;
		mapPrices.clear();
		for (HistoricalPrice hPrice : prices) {
			if (hPrice.getYear() == null || hPrice.getYear().toString().isEmpty()) {
				prices.remove(hPrice);
			} else {
				mapPrices.put(hPrice.getYear(), hPrice);
			}
		}
	}

	public CurrencyEnum getCurrency() {
		return currency;
	}

	public String toString() {
		return ticker;
	}

	@Override
	public int compareTo(Object arg0) {
		if (!(arg0 instanceof Symbol)) {
			throw new ClassCastException();
		}
		return getTicker().compareTo(((Symbol) arg0).getTicker());
	}

	

	@Override
    public boolean equals(Object obj) {
	    if (this == obj)
		    return true;
	    if (obj == null)
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    Symbol other = (Symbol) obj;
	    if (id == null) {
		    if (other.id != null)
			    return false;
	    } else if (!id.equals(other.id))
		    return false;
	    if (ticker == null) {
		    if (other.ticker != null)
			    return false;
	    } else if (!ticker.equals(other.ticker))
		    return false;
	    return true;
    }

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((id == null) ? 0 : id.hashCode());
	    result = prime * result + ((ticker == null) ? 0 : ticker.hashCode());
	    return result;
    }
}
