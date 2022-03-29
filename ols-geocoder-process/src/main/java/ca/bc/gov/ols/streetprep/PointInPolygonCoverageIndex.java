package ca.bc.gov.ols.streetprep;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.index.strtree.STRtree;


public class PointInPolygonCoverageIndex<T> {

	private STRtree index;
	private Function<T,Geometry> getGeom;
	
	public PointInPolygonCoverageIndex(Function<T,Geometry> getGeomFunc) {
		index = new STRtree();
		getGeom = getGeomFunc;
	}
	
	public void insert(T item) {
		Geometry g = getGeom.apply(item);
		IndexEntry entry = new IndexEntry(g, item);
		index.insert(g.getEnvelopeInternal(), entry);
	}
	
	public void build() {
		index.build();
	}
	
	public T queryOne(Point p) {
		@SuppressWarnings("unchecked")
		List<IndexEntry> list = index.query(p.getEnvelopeInternal());
		for(IndexEntry entry : list) {
			if(entry.pipIndex.locate(p.getCoordinate()) == Location.INTERIOR) {
				return entry.item;
			}
		}
		return null;
	}

	public List<T> query(Envelope env) {
		@SuppressWarnings("unchecked")
		List<IndexEntry> list = index.query(env);
		List<T> results = new ArrayList<T>();
		for(IndexEntry entry : list) {
			results.add(entry.item);
		}
		return results;
	}

	class IndexEntry {
		IndexedPointInAreaLocator pipIndex;
		T item;
		
		IndexEntry(Geometry g, T item) {
			pipIndex = new IndexedPointInAreaLocator(g);
			this.item = item;
		}
	}
}
