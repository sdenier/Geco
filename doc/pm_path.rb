class String
  def lv_matrix(other)
    a, b = self.unpack('U*'), other.unpack('U*')
#     puts "  " << (" " << self).split("").join("  ")
#     o = (" " << other).split("")
    a.lv_matrix(b)
  end
end


class Array

################################################################################
# Levenshtein adapté au comptage des manquants
#
  def pm_matrix(other)
    puts "h: " << self.join(" ")
    puts "v: " << other.join(" ")
    
    a, b = self, other
    n, m = a.length, b.length
    current = []
    current[0] = [*0..n]
    1.upto(m) do |i|
      current[i] = [i]+[0]*n
    end

    1.upto(m) do |i|
      1.upto(n) do |j|
        add, delete = current[i-1][j]+0, current[i][j-1]+1
        current[i][j] = current[i-1][j-1]
        current[i][j] = [add, delete].min if a[j-1] != b[i-1]
# Levenshtein original
#         change = current[i-1][j-1]
#         change += 1 if a[j-1] != b[i-1]
#         current[i][j] = [add, delete, change].min
      end
    end
    current.each{|row| puts " #{row.join(" ")}"}
    return current
  end


################################################################################
# Comptage des manquants par récursion double sur les listes
# Source original : Yoann OCAML
# Version non optimisée par mémoization
  def count_pm(trace)
    return 0 if( self.empty? )
    return self.length  if( trace.empty? )
    x,y = self.head, trace.head
    xs,ys = self.tail, trace.tail
    if( x==y )
      return xs.count_pm(ys)
    else
      return [self.count_pm(ys), 1 + xs.count_pm(trace)].min
    end
  end
  
  def head
    return self[0]
  end
  def tail
    copy = self + [] # copy array by union
    copy.shift
    return copy
  end


################################################################################
# Comptage des manquants par récursion double sur les listes
# Source original : Yoann OCAML
# Memoization
  def memocount_pm(trace, memo)
    if( !memo.has_key?(self) )
      memo[self] = {}
    end
    if( memo[self].has_key?(trace) )
      return memo[self][trace]
    else
      memo[self][trace] = bcount_pm(trace, memo)
      return memo[self][trace]
    end
  end

  def bcount_pm(trace, memo)
    return 0 if( self.empty? )
    return self.length  if( trace.empty? )
    x,y = self.head, trace.head
    xs,ys = self.tail, trace.tail
    if( x==y )
      return xs.memocount_pm(ys, memo)
    else
      return [self.memocount_pm(ys, memo), 1 + xs.memocount_pm(trace, memo)].min
    end
  end


##################################################################################
# Edit path based on lcss_matrix
#
  def edit_path(trace)
    lclen = self.lcss_matrix(trace)
    path = []
    n, m = self.length-1, trace.length-1
    
    while( n>=0 && m>=0 )
      if( self[n]==trace[m] )
        path.unshift(self[n].to_s)
        n, m = n-1, m-1
      else # ! décalage des indices, +1
        maxprv = [ lclen[m][n], lclen[m+1][n], lclen[m][n+1] ].max
        case maxprv
        when lclen[m][n]: path.unshift("s" + self[n].to_s + "/" + trace[m].to_s); n, m = n-1, m-1
        when lclen[m][n+1]: path.unshift("a" + trace[m].to_s); m = m-1
        when lclen[m+1][n]: path.unshift("d" + self[n].to_s); n = n-1
        end
      end
    end
    while( n>=0 )
      path.unshift("d" + self[n].to_s); n = n-1
    end
    while( m>=0 )
      path.unshift("a" + trace[m].to_s); m = m-1
    end
    return path
  end


################################################################################
# Longest common subsequence
#
  def lcss_matrix(trace)
    a, b = self, trace
    n, m = a.length, b.length
    puts "h: " << self.join(" ")
    puts "v: " << trace.join(" ")
    current = []
    0.upto(m) do |i|
      current[i] = [0]*(n+1)
    end
    
    1.upto(m) do |i|
      1.upto(n) do |j|
        clen = current[i-1][j-1] + 1
        if( a[j-1] != b[i-1] || rematching(clen, i, j)  ) # chars diff, or rematching current char too many times
          current[i][j] = [ current[i-1][j-1], current[i-1][j], current[i][j-1] ].max
        else # longest sequence (in context) len += 1
          current[i][j] = clen
        end
      end
    end
    # current.each{|row| puts " #{row.join(" ")}"}
    return current
  end

  def rematching(clen, i, j)
    return clen > (1 + [i, j].min) # clen > taille chaîne min
#     return (i<j && clen > (j+1)) || # triangle supérieur matrice
#       (i>j && clen > (i+1)) # triangle inférieur matrice
  end

  
end # Class Array/PM


################################################################################
# TEST/EXEMPLES

puts "\nMatrice de Levenshtein adapté"
"abada".lv_matrix("abdae")

#puts "\nPM : " << [131, 147, 131, 158, 131, 154, 131, 136].count_pm([131, 147, 158, 131, 131, 154, 131, 137, 13]).to_s
puts "\nPM : " << [131, 147, 131, 158, 131, 154, 131, 136].memocount_pm([131, 147, 158, 131, 131, 154, 131, 137, 13], {}).to_s

puts "Path : " << [131, 147, 131, 158, 131, 154, 131, 136].edit_path([131, 147, 158, 131, 131, 154, 131, 137, 13]).join(" ")


